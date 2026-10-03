package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.RecepcionEmergencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecepcionEmergencia_Repositorio
        extends JpaRepository<RecepcionEmergencia, Long> {

    List<RecepcionEmergencia>
    findByPacienteIdOrderByFechaCreacionDesc(
            Long pacienteId
    );
}
