package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.model.Cita;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MisCitasService {

    private final Cita_Repositorio citaRepository;


    public MisCitasService(
            Cita_Repositorio citaRepository
    ) {

        this.citaRepository = citaRepository;
    }


    // =====================================================
    // OBTENER CITAS DEL PACIENTE
    // =====================================================

    public List<Cita> obtenerCitasPaciente(
            Long pacienteId
    ) {

        return citaRepository
                .findByPacienteIdOrderByFechaCreacionDesc(
                        pacienteId
                );
    }
}
