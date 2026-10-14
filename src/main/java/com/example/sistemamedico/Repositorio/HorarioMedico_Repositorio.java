
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.HorarioMedico;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface HorarioMedico_Repositorio
        extends JpaRepository<HorarioMedico, Long> {

    // =====================================================
    // CONSULTAR HORARIOS POR SUCURSAL Y ESPECIALIDAD
    // =====================================================

    List<HorarioMedico>
    findBySucursalIdAndEspecialidadIdAndEstadoOrderByFechaAscHoraInicioAsc(
            Long sucursalId,
            Long especialidadId,
            String estado
    );

    // =====================================================
    // CONSULTAR HORARIOS DISPONIBLES DE UN MÉDICO
    // =====================================================

    List<HorarioMedico>
    findByMedicoIdAndSucursalIdAndEspecialidadIdAndEstadoOrderByFechaAscHoraInicioAsc(
            Long medicoId,
            Long sucursalId,
            Long especialidadId,
            String estado
    );

    // =====================================================
    // CONSULTAR HORARIOS POR MÉDICO Y FECHA
    // =====================================================

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

    // =====================================================
    // CU-16
    // BLOQUEAR UN HORARIO PARA SU RESERVA
    //
    // Debe llamarse dentro de una transacción activa.
    // PostgreSQL utilizará SELECT FOR UPDATE.
    // =====================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT h
            FROM HorarioMedico h
            WHERE h.id = :horarioId
            """)
    Optional<HorarioMedico> bloquearPorId(
            @Param("horarioId") Long horarioId
    );
}
