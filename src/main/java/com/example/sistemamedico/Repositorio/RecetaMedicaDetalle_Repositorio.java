package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.RecetaMedicaDetalle;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RecetaMedicaDetalle_Repositorio
        extends JpaRepository<RecetaMedicaDetalle, Long> {


    @EntityGraph(attributePaths = "medicamento")
    List<RecetaMedicaDetalle>
    findByRecetaIdOrderByIdAsc(
            Long recetaId
    );
}