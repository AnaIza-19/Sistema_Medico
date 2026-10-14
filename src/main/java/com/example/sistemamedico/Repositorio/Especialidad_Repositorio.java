
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Especialidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface Especialidad_Repositorio
        extends JpaRepository<Especialidad, Long> {


    // ==========================================
    // CU-03 Y CU-13
    // ESPECIALIDADES ACTIVAS POR SUCURSAL
    // CONSERVAR FUNCIONALIDAD EXISTENTE
    // ==========================================

    @Query("""
        SELECT e
        FROM Especialidad e
        JOIN SucursalEspecialidad se
            ON se.especialidad.id = e.id
        WHERE se.sucursal.id = :sucursalId
        AND se.activo = true
        AND e.activo = true
        ORDER BY e.nombre ASC
        """)
    List<Especialidad> findEspecialidadesActivasPorSucursal(
            @Param("sucursalId")
            Long sucursalId
    );


    // ==========================================
    // ESPECIALIDADES ACTIVAS
    // ==========================================

    List<Especialidad> findByActivoTrue();


    // ==========================================
    // CU-14
    // BUSCAR ESPECIALIDADES POR NOMBRE
    // CON PAGINACIÓN
    // ==========================================

    Page<Especialidad> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );


    // ==========================================
    // CU-14
    // VALIDAR NOMBRE DUPLICADO AL CREAR
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );


    // ==========================================
    // CU-14
    // VALIDAR NOMBRE DUPLICADO AL EDITAR
    // EXCLUYENDO EL REGISTRO ACTUAL
    // ==========================================

    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
            String nombre,
            Long id
    );

}
