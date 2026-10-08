export interface Categoria {
  id: number;
  nombre: string;
}

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precioCosto: number;
  precioVenta: number;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: string;
  categoriaId: number;
  categoria: string;
  estado: 'En stock' | 'Bajo stock' | 'Sin stock';
  activo: boolean;
}

export interface NuevoProducto {
  codigo: string;
  nombre: string;
  descripcion: string;
  precioCosto: number;
  precioVenta: number;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: string;
  categoriaId: number;
}
