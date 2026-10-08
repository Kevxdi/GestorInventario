import { CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Categoria, NuevoProducto, Producto } from '../models/producto';
import { ProductoService } from '../services/producto.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, FormsModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  readonly formulario = inject(FormBuilder).nonNullable.group({
    codigo: ['', [Validators.required, Validators.maxLength(30)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: [''],
    precioCosto: [0, [Validators.required, Validators.min(0)]],
    precioVenta: [0, [Validators.required, Validators.min(0)]],
    stockActual: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    stockMinimo: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    unidadMedida: ['', [Validators.required, Validators.maxLength(20)]],
    categoriaId: [0, [Validators.required, Validators.min(1)]]
  });

  productos: Producto[] = [];
  categorias: Categoria[] = [];
  categoriaSeleccionada = '';
  terminoBusqueda = '';
  mostrarArchivados = false;
  modalAbierto = false;
  cargando = true;
  guardando = false;
  productoEnAccionId: number | null = null;
  productoEditando: Producto | null = null;
  mensajeError = '';
  mensajeExito = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  get productosFiltrados(): Producto[] {
    const termino = this.terminoBusqueda.trim().toLowerCase();

    return this.productos.filter((producto) => {
      const coincideEstado = this.mostrarArchivados ? !producto.activo : producto.activo;
      const coincideCategoria = !this.categoriaSeleccionada
        || producto.categoria === this.categoriaSeleccionada;
      const coincideBusqueda = !termino
        || producto.nombre.toLowerCase().includes(termino)
        || producto.codigo.toLowerCase().includes(termino);

      return coincideEstado && coincideCategoria && coincideBusqueda;
    });
  }

  get totalStock(): number {
    return this.productosActivosListado.reduce((total, producto) => total + producto.stockActual, 0);
  }

  get productosBajos(): number {
    return this.productosActivosListado.filter((producto) => producto.estado === 'Bajo stock').length;
  }

  get productosActivos(): number {
    return this.productosActivosListado.filter((producto) => producto.stockActual > 0).length;
  }

  cantidadPorCategoria(categoria: string): number {
    return this.productosActivosListado.filter((producto) => producto.categoria === categoria).length;
  }

  abrirModal(): void {
    this.productoEditando = null;
    this.formulario.reset();
    this.mensajeError = '';
    this.mensajeExito = '';
    this.modalAbierto = true;
  }

  editarProducto(producto: Producto): void {
    this.productoEditando = producto;
    this.formulario.patchValue({
      codigo: producto.codigo,
      nombre: producto.nombre,
      descripcion: producto.descripcion ?? '',
      precioCosto: producto.precioCosto,
      precioVenta: producto.precioVenta,
      stockActual: producto.stockActual,
      stockMinimo: producto.stockMinimo,
      unidadMedida: producto.unidadMedida,
      categoriaId: producto.categoriaId
    });
    this.mensajeError = '';
    this.mensajeExito = '';
    this.modalAbierto = true;
  }

  cerrarModal(): void {
    if (this.guardando) {
      return;
    }

    this.modalAbierto = false;
    this.formulario.reset();
    this.productoEditando = null;
    this.mensajeError = '';
  }

  guardarProducto(): void {
    if (this.formulario.invalid || this.guardando) {
      this.formulario.markAllAsTouched();
      return;
    }

    const producto: NuevoProducto = this.formulario.getRawValue();
    const productoEditando = this.productoEditando;
    this.guardando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    const solicitud = productoEditando
      ? this.productoService.actualizarProducto(productoEditando.id, producto)
      : this.productoService.guardarProducto(producto);

    solicitud.subscribe({
      next: () => {
        this.guardando = false;
        this.modalAbierto = false;
        this.formulario.reset();
        this.productoEditando = null;
        this.mensajeExito = productoEditando
          ? 'Producto actualizado correctamente.'
          : 'Producto guardado correctamente.';
        this.cargarDatos();
        this.changeDetector.markForCheck();
      },
      error: (error: { error?: { detail?: string; message?: string }; message?: string }) => {
        this.guardando = false;
        this.mensajeError = error.error?.detail
          ?? error.error?.message
          ?? 'No se pudo guardar el producto. Verifica la conexión e inténtalo de nuevo.';
        this.changeDetector.markForCheck();
      }
    });
  }

  eliminarProducto(producto: Producto): void {
    const confirmar = window.confirm(
      `¿Eliminar "${producto.nombre}"? Si tiene movimientos, se archivará para conservar el historial.`
    );
    if (!confirmar || this.productoEnAccionId !== null) {
      return;
    }

    this.productoEnAccionId = producto.id;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.productoService.eliminarProducto(producto.id).subscribe({
      next: (resultado) => {
        this.productoEnAccionId = null;
        this.mensajeExito = resultado.mensaje;
        this.cargarDatos();
        this.changeDetector.markForCheck();
      },
      error: (error: { error?: { detail?: string; message?: string } }) => {
        this.productoEnAccionId = null;
        this.mensajeError = error.error?.detail
          ?? error.error?.message
          ?? 'No se pudo eliminar o archivar el producto.';
        this.changeDetector.markForCheck();
      }
    });
  }

  restaurarProducto(producto: Producto): void {
    if (this.productoEnAccionId !== null) {
      return;
    }

    this.productoEnAccionId = producto.id;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.productoService.actualizarEstado(producto.id, true).subscribe({
      next: () => {
        this.productoEnAccionId = null;
        this.mensajeExito = 'Producto restaurado correctamente.';
        this.cargarDatos();
        this.changeDetector.markForCheck();
      },
      error: (error: { error?: { detail?: string; message?: string } }) => {
        this.productoEnAccionId = null;
        this.mensajeError = error.error?.detail
          ?? error.error?.message
          ?? 'No se pudo restaurar el producto.';
        this.changeDetector.markForCheck();
      }
    });
  }

  cargarProductosDesdeFiltro(): void {
    this.cargarProductos();
  }

  private get productosActivosListado(): Producto[] {
    return this.productos.filter((producto) => producto.activo);
  }

  private cargarDatos(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.cargarProductos();

    this.productoService.obtenerCategorias().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las categorías desde el servidor.';
        this.changeDetector.markForCheck();
      }
    });
  }

  private cargarProductos(): void {
    this.cargando = true;
    this.productoService.obtenerProductos(this.mostrarArchivados).subscribe({
      next: (productos) => {
        this.productos = productos;
        this.cargando = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.cargando = false;
        this.mensajeError = 'No se pudieron cargar los productos. Comprueba que el servidor esté activo.';
        this.changeDetector.markForCheck();
      }
    });
  }
}
