package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.RecetaMedica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaMedica_Repositorio
        extends JpaRepository<RecetaMedica, Long> {

    List<RecetaMedica>
    findByCitaIdOrderByFechaCreacionDesc(Long citaId);
}
