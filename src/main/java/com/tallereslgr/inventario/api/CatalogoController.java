package com.tallereslgr.inventario.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class CatalogoController {

    private final CatalogoService servicio;

    public CatalogoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoCatalogo> obtenerProductos() {
        return servicio.obtenerProductos();
    }

    @GetMapping("/categorias")
    public List<String> obtenerCategorias() {
        return servicio.obtenerCategorias();
    }

    @GetMapping("/buscar")
    public List<ProductoCatalogo> buscarProductos(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "termino", required = false) String termino) {
        return servicio.buscarProductos(categoria, termino);
    }
}
