package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.PagoLaboratorio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PagoLaboratorio_Repositorio
        extends JpaRepository<PagoLaboratorio, Long> {


    // =====================================================
    // BUSCAR PAGO POR ORDEN
    // =====================================================

    Optional<PagoLaboratorio>
    findByOrdenId(
            Long ordenId
    );


    // =====================================================
    // SABER SI LA ORDEN YA FUE PAGADA
    // =====================================================

    boolean existsByOrdenId(
            Long ordenId
    );


    // =====================================================
    // BUSCAR POR NÚMERO DE TRANSACCIÓN
    // =====================================================

    Optional<PagoLaboratorio>
    findByNumeroTransaccion(
            String numeroTransaccion
    );
}