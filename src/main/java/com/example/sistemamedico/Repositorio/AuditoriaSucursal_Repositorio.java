package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaSucursal_Repositorio extends JpaRepository<AuditoriaSucursal, Long> {
    List<AuditoriaSucursal> findAllByOrderByFechaHoraDesc();
    List<AuditoriaSucursal> findBySucursalIdOrderByFechaHoraDesc(Long sucursalId);
    List<AuditoriaSucursal> findByEjecutadoPorOrderByFechaHoraDesc(String ejecutadoPor);
}
