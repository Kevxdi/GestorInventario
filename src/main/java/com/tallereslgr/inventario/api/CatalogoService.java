package com.tallereslgr.inventario.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CatalogoService {

    private final List<ProductoCatalogo> productos = List.of(
            new ProductoCatalogo("PRD-001", "Destornillador", "Herramientas", 24, new BigDecimal("18.50"), "En stock"),
            new ProductoCatalogo("PRD-002", "Taladro eléctrico", "Herramientas", 7, new BigDecimal("349.99"), "Bajo stock"),
            new ProductoCatalogo("PRD-003", "Nivel de burbuja", "Herramientas", 0, new BigDecimal("89.50"), "Sin stock"),
            new ProductoCatalogo("PRD-004", "Martillo de uña", "Herramientas", 12, new BigDecimal("45.00"), "En stock"),
            new ProductoCatalogo("PRD-005", "Alicates universales", "Herramientas", 18, new BigDecimal("65.00"), "En stock"),
            new ProductoCatalogo("PRD-006", "Cinta métrica", "Medición", 14, new BigDecimal("120.00"), "En stock")
    );

    public List<ProductoCatalogo> obtenerProductos() {
        return productos;
    }

    public List<String> obtenerCategorias() {
        return productos.stream()
                .map(ProductoCatalogo::categoria)
                .distinct()
                .sorted()
                .toList();
    }

    public List<ProductoCatalogo> buscarProductos(String categoria, String termino) {
        String normalizado = termino == null ? "" : termino.trim().toLowerCase();

        return productos.stream()
                .filter(producto -> categoria == null || categoria.isBlank() || producto.categoria().equalsIgnoreCase(categoria))
                .filter(producto -> normalizado.isEmpty()
                        || producto.nombre().toLowerCase().contains(normalizado)
                        || producto.codigo().toLowerCase().contains(normalizado))
                .toList();
    }
}
