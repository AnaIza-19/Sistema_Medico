package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface Cita_Repositorio
        extends JpaRepository<Cita, Long> {


    // =====================================================
    // CITAS DEL PACIENTE
    // =====================================================

    List<Cita>
    findByPacienteIdOrderByFechaCreacionDesc(
            Long pacienteId
    );


    // =====================================================
    // VERIFICAR SI UN HORARIO YA TIENE UNA CITA
    // CON DETERMINADO ESTADO
    // =====================================================

    boolean existsByHorarioIdAndEstado(
            Long horarioId,
            String estado
    );


    // =====================================================
    // CU-07
    //
    // PACIENTES QUE YA REGISTRARON SU LLEGADA
    // EN RECEPCIÓN Y ESTÁN ESPERANDO SER LLAMADOS
    // POR ENFERMERÍA.
    //
    // Se filtran por sucursal del enfermero.
    // =====================================================

    List<Cita>
    findByEstadoIgnoreCaseAndSucursalIdOrderByHoraLlegadaAsc(
            String estado,
            Long sucursalId
    );


    // =====================================================
    // CU-07
    //
    // CONSULTA GENERAL POR ESTADO.
    //
    // Nos será útil en pruebas y otros módulos.
    // =====================================================

    List<Cita>
    findByEstadoIgnoreCaseOrderByHoraLlegadaAsc(
            String estado
    );


    // =====================================================
    // CU-08
    //
    // CITAS ASIGNADAS A UN MÉDICO SEGÚN SU ESTADO.
    //
    // Permite obtener las citas del médico en:
    //
    // - LISTO_CONSULTA
    // - CONSULTA_MEDICA
    // - EVALUADO_PENDIENTE_CIERRE
    //
    // Ordenadas por hora de llegada.
    // =====================================================

    List<Cita>
    findByMedicoIdAndEstadoIgnoreCaseOrderByHoraLlegadaAsc(
            Long medicoId,
            String estado
    );


    // =====================================================
    // CU-06 FA03
    //
    // BUSCAR CITAS DEL PORTAL QUE:
    //
    // - Estén PENDIENTE_PAGO
    // - Sean origen PORTAL
    // - Tengan una reserva con fecha de expiración
    // - La reserva ya haya vencido
    //
    // Estas citas serán procesadas automáticamente.
    // =====================================================

    @Query("""
            SELECT c
            FROM Cita c
            JOIN FETCH c.horario h
            WHERE UPPER(c.estado) = 'PENDIENTE_PAGO'
              AND UPPER(c.origen) = 'PORTAL'
              AND h.reservadoHasta IS NOT NULL
              AND h.reservadoHasta < :ahora
            """)
    List<Cita> buscarCitasPortalVencidas(
            @Param("ahora")
            LocalDateTime ahora
    );
}