package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.SucursalEspecialidad;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SucursalEspecialidad_Repositorio
        extends JpaRepository<SucursalEspecialidad, Long> {


    // =====================================================
    // CU-13
    // LISTADO PAGINADO
    // =====================================================

    Page<SucursalEspecialidad> findAll(
            Pageable pageable
    );


    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    Optional<SucursalEspecialidad> findById(
            Long id
    );


    // =====================================================
    // VALIDAR DUPLICADO
    // SUCURSAL + ESPECIALIDAD
    // =====================================================

    boolean existsBySucursalIdAndEspecialidadId(
            Long sucursalId,
            Long especialidadId
    );


    // =====================================================
    // BUSCAR RELACIÓN
    // SUCURSAL + ESPECIALIDAD
    // =====================================================

    Optional<SucursalEspecialidad>
    findBySucursalIdAndEspecialidadId(
            Long sucursalId,
            Long especialidadId
    );


    // =====================================================
    // RELACIONES ACTIVAS POR SUCURSAL
    //
    // También podrá reutilizarse en el agendamiento
    // de citas.
    // =====================================================

    List<SucursalEspecialidad>
    findBySucursalIdAndActivoTrueOrderByEspecialidadNombreAsc(
            Long sucursalId
    );


    // =====================================================
    // RELACIONES ACTIVAS
    // =====================================================

    List<SucursalEspecialidad>
    findByActivoTrueOrderBySucursalNombreAscEspecialidadNombreAsc();

}
