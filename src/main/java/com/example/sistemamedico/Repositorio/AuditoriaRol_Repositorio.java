package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaRol_Repositorio extends JpaRepository<AuditoriaRol, Long> {
    List<AuditoriaRol> findByRolIdOrderByFechaHoraDesc(Long rolId);
}
