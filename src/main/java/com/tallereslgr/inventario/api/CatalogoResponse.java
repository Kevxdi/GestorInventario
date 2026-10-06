package com.tallereslgr.inventario.api;

import java.util.List;

public record CatalogoResponse(
        List<ProductoCatalogo> productos,
        List<String> categorias
) {
}
