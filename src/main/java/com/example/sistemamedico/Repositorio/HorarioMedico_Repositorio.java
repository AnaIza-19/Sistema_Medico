package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.HorarioMedico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface HorarioMedico_Repositorio
        extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico>
    findBySucursalIdAndEspecialidadIdAndEstadoOrderByFechaAscHoraInicioAsc(
            Long sucursalId,
            Long especialidadId,
            String estado
    );


    List<HorarioMedico>
    findByMedicoIdAndSucursalIdAndEspecialidadIdAndEstadoOrderByFechaAscHoraInicioAsc(
            Long medicoId,
            Long sucursalId,
            Long especialidadId,
            String estado
    );


    List<HorarioMedico>
    findByMedicoIdAndFechaAndEstadoOrderByHoraInicioAsc(
            Long medicoId,
            LocalDate fecha,
            String estado
    );


    // =====================================================
    // CU-05 FA07
    // BUSCAR MISMO HORARIO PARA OTRO MÉDICO
    // =====================================================

    Optional<HorarioMedico>
    findByMedicoIdAndSucursalIdAndEspecialidadIdAndFechaAndHoraInicioAndHoraFinAndEstado(
            Long medicoId,
            Long sucursalId,
            Long especialidadId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String estado
    );
}