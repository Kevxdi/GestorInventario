package com.tallereslgr.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tallereslgr.inventario.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNombreAsc();
}
