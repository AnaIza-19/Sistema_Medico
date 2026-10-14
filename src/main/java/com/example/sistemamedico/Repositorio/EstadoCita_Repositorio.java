
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.EstadoCita;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoCita_Repositorio
        extends JpaRepository<EstadoCita, Long> {

    List<EstadoCita> findByActivoTrueOrderByIdAsc();

    Page<EstadoCita> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );

    Optional<EstadoCita> findByCodigoIgnoreCase(
            String codigo
    );

    boolean existsByCodigoIgnoreCase(
            String codigo
    );

    boolean existsByNombreIgnoreCaseAndActivoTrue(
            String nombre
    );

    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
            String nombre,
            Long id
    );
}
