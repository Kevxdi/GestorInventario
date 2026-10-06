package com.tallereslgr.inventario.api;

import java.math.BigDecimal;

public record ProductoCatalogo(
        String codigo,
        String nombre,
        String categoria,
        int stock,
        BigDecimal precio,
        String estado
) {
}
