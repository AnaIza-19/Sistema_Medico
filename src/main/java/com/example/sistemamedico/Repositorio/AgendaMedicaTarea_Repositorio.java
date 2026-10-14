
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AgendaMedicaTarea;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendaMedicaTarea_Repositorio
        extends JpaRepository<AgendaMedicaTarea, Long> {

    // Todas las tareas del médico.
    List<AgendaMedicaTarea>
    findByMedicoIdOrderByFechaLimiteAsc(Long medicoId);

    // Tareas pendientes o completadas.
    List<AgendaMedicaTarea>
    findByMedicoIdAndIsCompletedOrderByFechaLimiteAsc(
            Long medicoId,
            Boolean isCompleted
    );

    // Tareas de una fecha seleccionada.
    List<AgendaMedicaTarea>
    findByMedicoIdAndFechaLimiteGreaterThanEqualAndFechaLimiteLessThanOrderByFechaLimiteAsc(
            Long medicoId,
            LocalDateTime inicio,
            LocalDateTime finExclusivo
    );

    // Verificar que la tarea pertenezca al médico.
    Optional<AgendaMedicaTarea>
    findByIdAndMedicoId(Long id, Long medicoId);
}
