
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Sucursal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Sucursal_Repositorio
        extends JpaRepository<Sucursal, Long> {


    // ==========================================
    // LISTAR SUCURSALES ACTIVAS
    // UTILIZADO POR CASOS ANTERIORES
    // ==========================================

    List<Sucursal> findByActivoTrue();


    // ==========================================
    // CU-14: BUSCAR POR NOMBRE
    // CON PAGINACIÓN
    // ==========================================

    Page<Sucursal> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );


    // ==========================================
    // CU-14: VALIDAR NOMBRE DUPLICADO
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );


    // ==========================================
    // CU-14: VALIDAR NOMBRE AL EDITAR
    // EXCLUYENDO LA MISMA SUCURSAL
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
            String nombre,
            Long id
    );

}
