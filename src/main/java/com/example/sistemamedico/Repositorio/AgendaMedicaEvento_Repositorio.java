
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AgendaMedicaEvento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendaMedicaEvento_Repositorio
        extends JpaRepository<AgendaMedicaEvento, Long> {

    // Eventos pertenecientes a un médico.
    List<AgendaMedicaEvento>
    findByMedicoIdOrderByFechaInicioAsc(Long medicoId);

    // Consultar un evento verificando su propietario.
    Optional<AgendaMedicaEvento>
    findByIdAndMedicoId(Long id, Long medicoId);

    // Eventos que se superponen al período consultado.
    List<AgendaMedicaEvento>
    findByMedicoIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
            Long medicoId,
            LocalDateTime finExclusivo,
            LocalDateTime inicio
    );
}
