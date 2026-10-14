package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaEstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaEstadoCita_Repositorio
        extends JpaRepository<AuditoriaEstadoCita, Long> {
    List<AuditoriaEstadoCita> findByEstadoCitaIdOrderByFechaHoraDesc(Long estadoCitaId);
    List<AuditoriaEstadoCita> findAllByOrderByFechaHoraDesc();
}
