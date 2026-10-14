package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaExamenLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaExamenLaboratorio_Repositorio
        extends JpaRepository<AuditoriaExamenLaboratorio, Long> {
    List<AuditoriaExamenLaboratorio> findAllByOrderByFechaHoraDesc();
    List<AuditoriaExamenLaboratorio> findByExamenIdOrderByFechaHoraDesc(Long examenId);
}
