package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaInventarioMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaInventarioMedicamento_Repositorio
        extends JpaRepository<AuditoriaInventarioMedicamento, Long> {
    List<AuditoriaInventarioMedicamento> findByInventarioIdOrderByIdDesc(Long inventarioId);
}
