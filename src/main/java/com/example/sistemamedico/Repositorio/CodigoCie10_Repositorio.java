package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.CodigoCie10;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CodigoCie10_Repositorio
        extends JpaRepository<CodigoCie10, Long> {


    // Buscar exactamente por código
    Optional<CodigoCie10>
    findByCodigoIgnoreCaseAndActivoTrue(
            String codigo
    );


    // Buscar mientras el médico escribe un código
    List<CodigoCie10>
    findTop10ByCodigoContainingIgnoreCaseAndActivoTrueOrderByCodigoAsc(
            String codigo
    );


    // Buscar también por descripción
    List<CodigoCie10>
    findTop10ByDescripcionContainingIgnoreCaseAndActivoTrueOrderByCodigoAsc(
            String descripcion
    );
}

