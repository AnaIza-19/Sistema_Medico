package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Pago;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Pago_Repositorio
        extends JpaRepository<Pago, Long> {

    Optional<Pago> findByNumeroTransaccion(
            String numeroTransaccion
    );

    Optional<Pago> findByIdempotencyKey(
            String idempotencyKey
    );

    Optional<Pago> findByCitaId(
            Long citaId
    );

    boolean existsByIdempotencyKey(
            String idempotencyKey
    );
}