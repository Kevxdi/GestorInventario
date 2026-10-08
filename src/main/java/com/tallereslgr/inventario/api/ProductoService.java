package com.tallereslgr.inventario.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tallereslgr.inventario.entity.Categoria;
import com.tallereslgr.inventario.entity.Producto;
import com.tallereslgr.inventario.repository.CategoriaRepository;
import com.tallereslgr.inventario.repository.ProductoRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final JdbcTemplate jdbcTemplate;

    public ProductoService(
            ProductoRepository productos,
            CategoriaRepository categorias,
            JdbcTemplate jdbcTemplate) {
        this.productos = productos;
        this.categorias = categorias;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductos(boolean incluirArchivados) {
        List<Producto> resultados = incluirArchivados
                ? productos.findAllByOrderByIdDesc()
                : productos.findAllByActivoTrueOrderByIdDesc();
        return resultados.stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(Long id) {
        Producto producto = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el producto solicitado."));
        return aRespuesta(producto);
    }

    public List<CategoriaOption> obtenerCategorias() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(categoria -> new CategoriaOption(categoria.getId(), categoria.getNombre()))
                .toList();
    }

    public List<String> obtenerNombresCategorias() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(Categoria::getNombre)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductos(String categoria, String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase();

        return obtenerProductos(false).stream()
                .filter(producto -> categoria == null || categoria.isBlank()
                        || producto.categoria().equalsIgnoreCase(categoria))
                .filter(producto -> busqueda.isEmpty()
                        || producto.nombre().toLowerCase().contains(busqueda)
                        || producto.codigo().toLowerCase().contains(busqueda))
                .toList();
    }

    @Transactional
    public ProductoResponse guardarProducto(ProductoRequest solicitud) {
        validar(solicitud);
        String codigo = solicitud.codigo().trim();

        if (productos.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código.");
        }

        Categoria categoria = categorias.findById(solicitud.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La categoría seleccionada no existe."));

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(solicitud.nombre().trim());
        producto.setDescripcion(limpiar(solicitud.descripcion()));
        producto.setPrecioCosto(solicitud.precioCosto());
        producto.setPrecioVenta(solicitud.precioVenta());
        producto.setStockActual(solicitud.stockActual());
        producto.setStockMinimo(solicitud.stockMinimo());
        producto.setUnidadMedida(solicitud.unidadMedida().trim());
        producto.setCategoria(categoria);
        producto.setFechaRegistro(LocalDateTime.now());

        return aRespuesta(productos.save(producto));
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long id, ProductoRequest solicitud) {
        validar(solicitud);
        Producto producto = buscarProducto(id);
        String codigo = solicitud.codigo().trim();

        if (productos.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro producto con ese código.");
        }

        Categoria categoria = categorias.findById(solicitud.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La categoría seleccionada no existe."));

        producto.setCodigo(codigo);
        aplicarDatos(producto, solicitud, categoria);
        return aRespuesta(productos.save(producto));
    }

    @Transactional
    public ProductoEliminacionResponse eliminarProducto(Long id) {
        Producto producto = buscarProducto(id);
        Long movimientos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movimientos WHERE producto_id = ?",
                Long.class,
                id);

        if (movimientos != null && movimientos > 0) {
            producto.setActivo(false);
            productos.save(producto);
            return new ProductoEliminacionResponse(
                    true, "El producto tiene movimientos y se archivó para conservar su historial.");
        }

        productos.delete(producto);
        return new ProductoEliminacionResponse(false, "El producto se eliminó correctamente.");
    }

    @Transactional
    public ProductoResponse actualizarEstado(Long id, boolean activo) {
        Producto producto = buscarProducto(id);
        producto.setActivo(activo);
        return aRespuesta(productos.save(producto));
    }

    private void validar(ProductoRequest solicitud) {
        if (solicitud == null
                || solicitud.codigo() == null || solicitud.codigo().isBlank()
                || solicitud.codigo().trim().length() > 30
                || solicitud.nombre() == null || solicitud.nombre().isBlank()
                || solicitud.nombre().trim().length() > 100
                || solicitud.descripcion() != null && solicitud.descripcion().length() > 500
                || solicitud.unidadMedida() == null || solicitud.unidadMedida().isBlank()
                || solicitud.unidadMedida().trim().length() > 20
                || solicitud.precioCosto() == null || solicitud.precioCosto().signum() < 0
                || solicitud.precioVenta() == null || solicitud.precioVenta().signum() < 0
                || solicitud.stockActual() == null || solicitud.stockActual() < 0
                || solicitud.stockMinimo() == null || solicitud.stockMinimo() < 0
                || solicitud.categoriaId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Revisa los campos obligatorios y sus valores.");
        }
    }

    private ProductoResponse aRespuesta(Producto producto) {
        String estado = producto.getStockActual() == 0
                ? "Sin stock"
                : producto.getStockActual() <= producto.getStockMinimo() ? "Bajo stock" : "En stock";

        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioCosto(),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getUnidadMedida(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                estado,
                producto.isActivo());
    }

    private Producto buscarProducto(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el producto solicitado."));
    }

    private void aplicarDatos(Producto producto, ProductoRequest solicitud, Categoria categoria) {
        producto.setNombre(solicitud.nombre().trim());
        producto.setDescripcion(limpiar(solicitud.descripcion()));
        producto.setPrecioCosto(solicitud.precioCosto());
        producto.setPrecioVenta(solicitud.precioVenta());
        producto.setStockActual(solicitud.stockActual());
        producto.setStockMinimo(solicitud.stockMinimo());
        producto.setUnidadMedida(solicitud.unidadMedida().trim());
        producto.setCategoria(categoria);
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
