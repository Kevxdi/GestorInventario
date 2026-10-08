import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { Categoria, Producto } from './models/producto';
import { ProductoService } from './services/producto.service';
import { DashboardComponent } from './dashboard/dashboard.component';

describe('DashboardComponent', () => {
  const productos: Producto[] = [
    {
      id: 1,
      codigo: 'PRD-001',
      nombre: 'Destornillador',
      descripcion: null,
      precioCosto: 10,
      precioVenta: 18.5,
      stockActual: 24,
      stockMinimo: 5,
      unidadMedida: 'piezas',
      categoriaId: 1,
      categoria: 'Herramientas',
      estado: 'En stock',
      activo: true
    },
    {
      id: 2,
      codigo: 'PRD-002',
      nombre: 'Taladro eléctrico',
      descripcion: null,
      precioCosto: 200,
      precioVenta: 349.99,
      stockActual: 7,
      stockMinimo: 10,
      unidadMedida: 'piezas',
      categoriaId: 1,
      categoria: 'Herramientas',
      estado: 'Bajo stock',
      activo: true
    },
    {
      id: 3,
      codigo: 'PRD-003',
      nombre: 'Nivel de burbuja',
      descripcion: null,
      precioCosto: 40,
      precioVenta: 89.5,
      stockActual: 0,
      stockMinimo: 3,
      unidadMedida: 'piezas',
      categoriaId: 1,
      categoria: 'Herramientas',
      estado: 'Sin stock',
      activo: true
    },
    {
      id: 4,
      codigo: 'PRD-004',
      nombre: 'Martillo de uña',
      descripcion: null,
      precioCosto: 20,
      precioVenta: 45,
      stockActual: 12,
      stockMinimo: 4,
      unidadMedida: 'piezas',
      categoriaId: 2,
      categoria: 'Medición',
      estado: 'En stock',
      activo: true
    }
  ];
  const categorias: Categoria[] = [
    { id: 1, nombre: 'Herramientas' },
    { id: 2, nombre: 'Medición' }
  ];
  let servicio: jasmine.SpyObj<ProductoService>;

  beforeEach(async () => {
    servicio = jasmine.createSpyObj<ProductoService>(
      'ProductoService',
      [
        'obtenerProductos',
        'obtenerCategorias',
        'guardarProducto',
        'actualizarProducto',
        'eliminarProducto',
        'actualizarEstado'
      ]
    );
    servicio.obtenerProductos.and.returnValue(of(productos));
    servicio.obtenerCategorias.and.returnValue(of(categorias));
    servicio.guardarProducto.and.returnValue(of(productos[0]));
    servicio.actualizarProducto.and.returnValue(of(productos[0]));
    servicio.eliminarProducto.and.returnValue(of({
      archivado: false,
      mensaje: 'Producto eliminado correctamente.'
    }));
    servicio.actualizarEstado.and.returnValue(of(productos[0]));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ProductoService, useValue: servicio }
      ]
    }).compileComponents();
  });

  it('loads products from the service and renders inventory metrics', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(servicio.obtenerProductos).toHaveBeenCalled();
    expect(compiled.textContent).toContain('Total de productos');
    expect(compiled.textContent).toContain('Destornillador');
    expect(fixture.componentInstance.totalStock).toBe(43);
    expect(fixture.componentInstance.productosBajos).toBe(1);
  });

  it('filters products by category and search term', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.componentInstance.categoriaSeleccionada = 'Herramientas';
    fixture.componentInstance.terminoBusqueda = 'taladro';
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Taladro eléctrico');
    expect(compiled.textContent).not.toContain('Nivel de burbuja');
  });

  it('saves a new product, closes the modal, and reloads inventory', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.abrirModal();
    component.formulario.setValue({
      codigo: 'PRD-005',
      nombre: 'Alicate',
      descripcion: '',
      precioCosto: 20,
      precioVenta: 40,
      stockActual: 8,
      stockMinimo: 2,
      unidadMedida: 'piezas',
      categoriaId: 1
    });

    component.guardarProducto();

    expect(servicio.guardarProducto).toHaveBeenCalled();
    expect(servicio.obtenerProductos).toHaveBeenCalledTimes(2);
    expect(component.modalAbierto).toBeFalse();
    expect(component.formulario.pristine).toBeTrue();
  });

  it('edits a product using the selected product id and reloads inventory', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.editarProducto(productos[0]);
    component.formulario.patchValue({ nombre: 'Destornillador actualizado' });

    component.guardarProducto();

    expect(servicio.actualizarProducto).toHaveBeenCalledWith(1, jasmine.objectContaining({
      codigo: 'PRD-001',
      nombre: 'Destornillador actualizado',
      categoriaId: 1
    }));
    expect(servicio.obtenerProductos).toHaveBeenCalledTimes(2);
    expect(component.modalAbierto).toBeFalse();
    expect(component.mensajeExito).toBe('Producto actualizado correctamente.');
  });
});
