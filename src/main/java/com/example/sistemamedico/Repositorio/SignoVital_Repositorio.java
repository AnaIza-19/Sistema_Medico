package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.SignoVital;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SignoVital_Repositorio
        extends JpaRepository<SignoVital, Long> {


    // =====================================================
    // BUSCAR SIGNOS VITALES POR CITA
    // =====================================================

    Optional<SignoVital>
    findByCitaId(
            Long citaId
    );


    // =====================================================
    // VERIFICAR SI YA EXISTEN SIGNOS VITALES
    // PARA UNA CITA
    // =====================================================

    boolean
    existsByCitaId(
            Long citaId
    );


    // =====================================================
    // HISTORIAL DE SIGNOS VITALES DEL PACIENTE
    // CU-07
    //
    // Relación:
    // SignoVital -> Cita -> Paciente
    // =====================================================

    List<SignoVital>
    findByCitaPacienteIdOrderByFechaRegistroDesc(
            Long pacienteId
    );
}
