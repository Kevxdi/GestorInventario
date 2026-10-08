package com.tallereslgr.inventario.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.tallereslgr.inventario.entity.Categoria;
import com.tallereslgr.inventario.entity.Producto;
import com.tallereslgr.inventario.repository.CategoriaRepository;
import com.tallereslgr.inventario.repository.ProductoRepository;

class ProductoServiceTest {

    private final ProductoRepository productos = org.mockito.Mockito.mock(ProductoRepository.class);
    private final CategoriaRepository categorias = org.mockito.Mockito.mock(CategoriaRepository.class);
    private final JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
    private final ProductoService servicio = new ProductoService(productos, categorias, jdbcTemplate);

    @Test
    void guardaProductoAsociadoALaCategoriaSeleccionada() {
        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNombre("Herramientas");
        when(productos.existsByCodigoIgnoreCase("PRD-007")).thenReturn(false);
        when(categorias.findById(3L)).thenReturn(Optional.of(categoria));
        when(productos.save(any(Producto.class))).thenAnswer(invocacion -> {
            Producto producto = invocacion.getArgument(0);
            producto.setId(12L);
            return producto;
        });

        ProductoResponse resultado = servicio.guardarProducto(new ProductoRequest(
                " PRD-007 ", "Taladro", "", new BigDecimal("100.00"), new BigDecimal("150.00"),
                4, 2, "piezas", 3L));

        assertEquals(12L, resultado.id());
        assertEquals("PRD-007", resultado.codigo());
        assertEquals("Herramientas", resultado.categoria());
        assertEquals("En stock", resultado.estado());
        assertEquals(true, resultado.activo());
        verify(productos).save(any(Producto.class));
    }

    @Test
    void actualizaProductoYPermiteConservarSuCodigo() {
        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNombre("Herramientas");
        Producto existente = new Producto();
        existente.setId(12L);
        existente.setCodigo("PRD-007");
        existente.setNombre("Taladro viejo");
        existente.setPrecioCosto(BigDecimal.ONE);
        existente.setPrecioVenta(BigDecimal.TEN);
        existente.setStockActual(1);
        existente.setStockMinimo(1);
        existente.setUnidadMedida("piezas");
        existente.setCategoria(categoria);
        when(productos.findById(12L)).thenReturn(Optional.of(existente));
        when(productos.existsByCodigoIgnoreCaseAndIdNot("PRD-007", 12L)).thenReturn(false);
        when(categorias.findById(3L)).thenReturn(Optional.of(categoria));
        when(productos.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoResponse resultado = servicio.actualizarProducto(12L, new ProductoRequest(
                "PRD-007", "Taladro", "Actualizado", new BigDecimal("100.00"),
                new BigDecimal("150.00"), 4, 2, "piezas", 3L));

        assertEquals("Taladro", resultado.nombre());
        assertEquals("Actualizado", resultado.descripcion());
        verify(productos).save(existente);
    }

    @Test
    void archivaProductoSiTieneMovimientos() {
        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNombre("Herramientas");
        Producto existente = new Producto();
        existente.setId(12L);
        existente.setCodigo("PRD-007");
        existente.setNombre("Taladro");
        existente.setStockActual(4);
        existente.setStockMinimo(2);
        existente.setCategoria(categoria);
        when(productos.findById(12L)).thenReturn(Optional.of(existente));
        when(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movimientos WHERE producto_id = ?", Long.class, 12L))
                .thenReturn(2L);
        when(productos.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoEliminacionResponse resultado = servicio.eliminarProducto(12L);

        assertEquals(true, resultado.archivado());
        assertEquals(false, existente.isActivo());
        verify(productos).save(existente);
        verify(productos, never()).delete(existente);
    }

    @Test
    void eliminaProductoSinMovimientos() {
        Producto existente = new Producto();
        existente.setId(12L);
        when(productos.findById(12L)).thenReturn(Optional.of(existente));
        when(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movimientos WHERE producto_id = ?", Long.class, 12L))
                .thenReturn(0L);

        ProductoEliminacionResponse resultado = servicio.eliminarProducto(12L);

        assertEquals(false, resultado.archivado());
        verify(productos).delete(existente);
    }

    @Test
    void rechazaCodigoDuplicado() {
        when(productos.existsByCodigoIgnoreCase("PRD-001")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> servicio.guardarProducto(new ProductoRequest(
                "PRD-001", "Taladro", null, BigDecimal.ONE, BigDecimal.TEN, 1, 1, "piezas", 3L)));
    }
}
