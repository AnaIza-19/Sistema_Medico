package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaMedicamento_Repositorio
        extends JpaRepository<AuditoriaMedicamento, Long> {

    List<AuditoriaMedicamento> findByMedicamentoIdOrderByFechaHoraDesc(Long medicamentoId);
}
