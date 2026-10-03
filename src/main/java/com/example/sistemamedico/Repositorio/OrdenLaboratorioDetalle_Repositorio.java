package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.OrdenLaboratorioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenLaboratorioDetalle_Repositorio
        extends JpaRepository<OrdenLaboratorioDetalle, Long> {

    List<OrdenLaboratorioDetalle>
    findByOrdenIdOrderByIdAsc(Long ordenId);
}
