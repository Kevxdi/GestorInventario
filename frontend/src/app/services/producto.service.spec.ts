import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { NuevoProducto, Producto } from '../models/producto';
import { ProductoService } from './producto.service';

describe('ProductoService', () => {
  let servicio: ProductoService;
  let solicitudes: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    servicio = TestBed.inject(ProductoService);
    solicitudes = TestBed.inject(HttpTestingController);
  });

  afterEach(() => solicitudes.verify());

  it('sends new products to the API and returns the saved product', () => {
    const nuevoProducto: NuevoProducto = {
      codigo: 'PRD-007',
      nombre: 'Taladro',
      descripcion: '',
      precioCosto: 100,
      precioVenta: 150,
      stockActual: 4,
      stockMinimo: 2,
      unidadMedida: 'piezas',
      categoriaId: 3
    };
    const productoGuardado: Producto = {
      id: 7,
      ...nuevoProducto,
      categoria: 'Herramientas',
      estado: 'En stock',
      activo: true
    };

    servicio.guardarProducto(nuevoProducto).subscribe((producto) => {
      expect(producto).toEqual(productoGuardado);
    });

    const solicitud = solicitudes.expectOne('http://localhost:8080/api/productos');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual(nuevoProducto);
    solicitud.flush(productoGuardado);
  });

  it('updates a product by id', () => {
    const cambios: NuevoProducto = {
      codigo: 'PRD-007',
      nombre: 'Taladro actualizado',
      descripcion: '',
      precioCosto: 100,
      precioVenta: 160,
      stockActual: 4,
      stockMinimo: 2,
      unidadMedida: 'piezas',
      categoriaId: 3
    };

    servicio.actualizarProducto(7, cambios).subscribe();

    const solicitud = solicitudes.expectOne('http://localhost:8080/api/productos/7');
    expect(solicitud.request.method).toBe('PUT');
    expect(solicitud.request.body).toEqual(cambios);
    solicitud.flush({ id: 7, ...cambios, categoria: 'Herramientas', estado: 'En stock', activo: true });
  });

  it('deletes or archives a product through the API', () => {
    servicio.eliminarProducto(7).subscribe((resultado) => {
      expect(resultado.archivado).toBeTrue();
    });

    const solicitud = solicitudes.expectOne('http://localhost:8080/api/productos/7');
    expect(solicitud.request.method).toBe('DELETE');
    solicitud.flush({ archivado: true, mensaje: 'Archivado para conservar historial.' });
  });

  it('changes a product active state', () => {
    servicio.actualizarEstado(7, true).subscribe();

    const solicitud = solicitudes.expectOne(
      (peticion) => peticion.url === 'http://localhost:8080/api/productos/7/estado'
    );
    expect(solicitud.request.method).toBe('PATCH');
    expect(solicitud.request.params.get('activo')).toBe('true');
    solicitud.flush({
      id: 7,
      codigo: 'PRD-007',
      nombre: 'Taladro',
      descripcion: '',
      precioCosto: 100,
      precioVenta: 150,
      stockActual: 4,
      stockMinimo: 2,
      unidadMedida: 'piezas',
      categoriaId: 3,
      categoria: 'Herramientas',
      estado: 'En stock',
      activo: true
    });
  });
});
