package com.tallereslgr.inventario.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CatalogoServiceTest {

    private final CatalogoService servicio = new CatalogoService();

    @Test
    void debeObtenerLasCategoriasUnicas() {
        assertEquals(List.of("Herramientas", "Medición"), servicio.obtenerCategorias());
    }

    @Test
    void debeFiltrarPorCategoriaYTermino() {
        List<ProductoCatalogo> resultados = servicio.buscarProductos("herramientas", "taladro");

        assertEquals(1, resultados.size());
        assertEquals("PRD-002", resultados.getFirst().codigo());
    }

    @Test
    void debeDevolverTodosLosProductosCuandoNoHayFiltros() {
        assertEquals(6, servicio.buscarProductos(null, null).size());
    }

    @Test
    void debeBuscarPorCodigo() {
        List<ProductoCatalogo> resultados = servicio.buscarProductos(null, "prd-005");

        assertEquals(1, resultados.size());
        assertTrue(resultados.getFirst().codigo().equalsIgnoreCase("PRD-005"));
    }
}
