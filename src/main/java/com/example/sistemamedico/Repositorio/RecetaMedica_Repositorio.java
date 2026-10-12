package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.RecetaMedica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RecetaMedica_Repositorio
        extends JpaRepository<RecetaMedica, Long> {


    // ==========================================
    // CU-08
    // RECETAS DE UNA CITA
    // ==========================================

    List<RecetaMedica>
    findByCitaIdOrderByFechaCreacionDesc(
            Long citaId
    );


    // ==========================================
    // CU-11
    // BUSCAR RECETA ACTIVA POR ID
    // ==========================================

    Optional<RecetaMedica>
    findByIdAndActivoTrue(
            Long id
    );


    // ==========================================
    // CU-11
    // BUSCAR RECETAS ACTIVAS POR CITA
    // ==========================================

    List<RecetaMedica>
    findByCitaIdAndActivoTrueOrderByFechaCreacionDesc(
            Long citaId
    );


    // ==========================================
    // CU-11
    // LISTAR RECETAS ACTIVAS
    // ==========================================

    List<RecetaMedica>
    findByActivoTrueOrderByFechaCreacionDesc();
}
