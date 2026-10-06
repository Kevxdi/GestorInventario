import { CurrencyPipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Producto {
  codigo: string;
  nombre: string;
  categoria: string;
  stock: number;
  precio: number;
  estado: 'En stock' | 'Bajo stock' | 'Sin stock';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  readonly productos: Producto[] = [
    { codigo: 'PRD-001', nombre: 'Destornillador', categoria: 'Herramientas', stock: 24, precio: 18.5, estado: 'En stock' },
    { codigo: 'PRD-002', nombre: 'Taladro eléctrico', categoria: 'Herramientas', stock: 7, precio: 349.99, estado: 'Bajo stock' },
    { codigo: 'PRD-003', nombre: 'Nivel de burbuja', categoria: 'Herramientas', stock: 0, precio: 89.5, estado: 'Sin stock' },
    { codigo: 'PRD-004', nombre: 'Martillo de uña', categoria: 'Herramientas', stock: 12, precio: 45, estado: 'En stock' }
  ];

  readonly categorias = [...new Set(this.productos.map((producto) => producto.categoria))].sort();
  categoriaSeleccionada = '';
  terminoBusqueda = '';

  readonly totalStock = this.productos.reduce((total, producto) => total + producto.stock, 0);
  readonly productosBajos = this.productos.filter((producto) => producto.stock <= 10).length;
  readonly productosActivos = this.productos.filter((producto) => producto.stock > 0).length;

  get productosFiltrados(): Producto[] {
    const termino = this.terminoBusqueda.trim().toLowerCase();

    return this.productos.filter((producto) => {
      const coincideCategoria = !this.categoriaSeleccionada
        || producto.categoria === this.categoriaSeleccionada;
      const coincideBusqueda = !termino
        || producto.nombre.toLowerCase().includes(termino)
        || producto.codigo.toLowerCase().includes(termino);

      return coincideCategoria && coincideBusqueda;
    });
  }

  cantidadPorCategoria(categoria: string): number {
    return this.productos.filter((producto) => producto.categoria === categoria).length;
  }
}
