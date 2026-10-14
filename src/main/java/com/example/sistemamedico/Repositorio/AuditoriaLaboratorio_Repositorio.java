package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaLaboratorio_Repositorio
        extends JpaRepository<AuditoriaLaboratorio, Long> {
    List<AuditoriaLaboratorio> findAllByOrderByFechaHoraDesc();
    List<AuditoriaLaboratorio> findByLaboratorioIdOrderByFechaHoraDesc(Long laboratorioId);
}
