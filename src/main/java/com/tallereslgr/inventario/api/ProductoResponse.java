package com.tallereslgr.inventario.api;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precioCosto,
        BigDecimal precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        String unidadMedida,
        Long categoriaId,
        String categoria,
        String estado,
        boolean activo
) {
}
