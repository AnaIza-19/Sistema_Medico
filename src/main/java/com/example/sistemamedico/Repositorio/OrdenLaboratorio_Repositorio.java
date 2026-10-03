package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.OrdenLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenLaboratorio_Repositorio
        extends JpaRepository<OrdenLaboratorio, Long> {

    List<OrdenLaboratorio>
    findByCitaIdOrderByFechaCreacionDesc(Long citaId);
}