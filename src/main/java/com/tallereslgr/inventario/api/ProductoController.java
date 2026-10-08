package com.tallereslgr.inventario.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(
        origins = {
                "http://localhost:4200",
                "http://localhost:4201",
                "http://localhost:4300",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:4201",
                "http://127.0.0.1:4300"
        },
        methods = {org.springframework.web.bind.annotation.RequestMethod.GET,
                org.springframework.web.bind.annotation.RequestMethod.POST,
                org.springframework.web.bind.annotation.RequestMethod.PUT,
                org.springframework.web.bind.annotation.RequestMethod.DELETE,
                org.springframework.web.bind.annotation.RequestMethod.PATCH,
                org.springframework.web.bind.annotation.RequestMethod.OPTIONS},
        allowedHeaders = "*"
)
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoResponse> obtenerProductos(
            @RequestParam(name = "incluirArchivados", defaultValue = "false") boolean incluirArchivados) {
        return servicio.obtenerProductos(incluirArchivados);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtenerProducto(@PathVariable Long id) {
        return servicio.obtenerProducto(id);
    }

    @GetMapping("/categorias")
    public List<String> obtenerNombresCategorias() {
        return servicio.obtenerNombresCategorias();
    }

    @GetMapping("/categorias/opciones")
    public List<CategoriaOption> obtenerCategorias() {
        return servicio.obtenerCategorias();
    }

    @GetMapping("/buscar")
    public List<ProductoResponse> buscarProductos(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "termino", required = false) String termino) {
        return servicio.buscarProductos(categoria, termino);
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> guardarProducto(@RequestBody ProductoRequest solicitud) {
        ProductoResponse producto = servicio.guardarProducto(solicitud);
        return ResponseEntity.created(URI.create("/api/productos/" + producto.id())).body(producto);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoRequest solicitud) {
        return servicio.actualizarProducto(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ProductoEliminacionResponse eliminarProducto(@PathVariable Long id) {
        return servicio.eliminarProducto(id);
    }

    @PatchMapping("/{id}/estado")
    public ProductoResponse actualizarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        return servicio.actualizarEstado(id, activo);
    }
}
