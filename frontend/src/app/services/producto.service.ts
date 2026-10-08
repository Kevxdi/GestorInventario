import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Categoria, NuevoProducto, Producto } from '../models/producto';

export interface ResultadoEliminacionProducto {
  archivado: boolean;
  mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/productos';

  obtenerProductos(incluirArchivados = false): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl, {
      params: incluirArchivados ? { incluirArchivados: true } : {}
    });
  }

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias/opciones`);
  }

  guardarProducto(producto: NuevoProducto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  actualizarProducto(id: number, producto: NuevoProducto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  eliminarProducto(id: number): Observable<ResultadoEliminacionProducto> {
    return this.http.delete<ResultadoEliminacionProducto>(`${this.apiUrl}/${id}`);
  }

  actualizarEstado(id: number, activo: boolean): Observable<Producto> {
    return this.http.patch<Producto>(`${this.apiUrl}/${id}/estado`, {}, {
      params: { activo }
    });
  }
}
