package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.ConsultaMedica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ConsultaMedica_Repositorio
        extends JpaRepository<ConsultaMedica, Long> {

    // Buscar la consulta médica asociada a una cita
    Optional<ConsultaMedica> findByCitaId(Long citaId);

    // Verificar si una cita ya tiene una consulta médica
    boolean existsByCitaId(Long citaId);

}
