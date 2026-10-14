package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Laboratorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Laboratorio_Repositorio extends JpaRepository<Laboratorio, Long> {
    Page<Laboratorio> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    List<Laboratorio> findByActivoTrueOrderByIdAsc();
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(String nombre, Long id);
}
