package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Sucursal_Repositorio extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivoTrue();
}
