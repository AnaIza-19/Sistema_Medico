package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.ExamenLaboratorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamenLaboratorio_Repositorio extends JpaRepository<ExamenLaboratorio, Long> {
    // Método existente utilizado por los módulos del laboratorio CU-09:
    List<ExamenLaboratorio> findByActivoTrueOrderByNombreAsc();

    // Búsqueda y paginación CU-14:
    Page<ExamenLaboratorio> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Coinciden con la restricción original UNIQUE(nombre), de alcance global:
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
