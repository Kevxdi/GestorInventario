package com.tallereslgr.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tallereslgr.inventario.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    List<Producto> findAllByOrderByIdDesc();

    List<Producto> findAllByActivoTrueOrderByIdDesc();
}
