package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Medicamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Medicamento_Repositorio
        extends JpaRepository<Medicamento, Long> {

    List<Medicamento>
    findByActivoTrueOrderByNombreAsc();
}
