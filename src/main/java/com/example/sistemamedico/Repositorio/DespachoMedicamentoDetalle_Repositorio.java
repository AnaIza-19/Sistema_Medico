package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.DespachoMedicamentoDetalle;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DespachoMedicamentoDetalle_Repositorio
        extends JpaRepository<DespachoMedicamentoDetalle, Long> {


    // =====================================================
    // DETALLES DE UN DESPACHO
    // =====================================================

    @EntityGraph(
            attributePaths = {
                    "medicamentoOriginal",
                    "medicamentoDespachado"
            }
    )
    List<DespachoMedicamentoDetalle>
    findByDespachoIdOrderByIdAsc(
            Long despachoId
    );


    // =====================================================
    // CU-11
    // DETALLES DE VARIOS DESPACHOS
    // PARA HISTORIAL DEL PACIENTE
    // =====================================================

    @EntityGraph(
            attributePaths = {
                    "despacho",
                    "medicamentoOriginal",
                    "medicamentoDespachado"
            }
    )
    List<DespachoMedicamentoDetalle>
    findByDespachoIdInOrderByDespachoFechaDespachoDescIdAsc(
            List<Long> despachoIds
    );
}