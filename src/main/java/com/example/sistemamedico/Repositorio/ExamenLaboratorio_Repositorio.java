package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.ExamenLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenLaboratorio_Repositorio
        extends JpaRepository<ExamenLaboratorio, Long> {

    List<ExamenLaboratorio>
    findByActivoTrueOrderByNombreAsc();
}