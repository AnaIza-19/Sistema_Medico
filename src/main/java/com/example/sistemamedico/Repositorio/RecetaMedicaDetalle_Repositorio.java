package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.RecetaMedicaDetalle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaMedicaDetalle_Repositorio
        extends JpaRepository<RecetaMedicaDetalle, Long> {

    List<RecetaMedicaDetalle>
    findByRecetaIdOrderByIdAsc(Long recetaId);
}