package com.tallereslgr.inventario.api;

import java.math.BigDecimal;

public record ProductoRequest(
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precioCosto,
        BigDecimal precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        String unidadMedida,
        Long categoriaId
) {
}
