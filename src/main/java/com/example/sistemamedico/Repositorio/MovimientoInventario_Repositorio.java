
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.MovimientoInventario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface MovimientoInventario_Repositorio
        extends JpaRepository<MovimientoInventario, Long>,
        JpaSpecificationExecutor<MovimientoInventario> {


    // =====================================================
    // LISTADO GENERAL
    // =====================================================

    Page<MovimientoInventario>
    findAllByOrderByFechaHoraDesc(Pageable pageable);


    // =====================================================
    // HISTORIAL DE UN INVENTARIO
    // =====================================================

    List<MovimientoInventario>
    findTop10ByInventarioIdOrderByFechaHoraDesc(
            Long inventarioId
    );


    // =====================================================
    // EVITAR DESPACHOS DUPLICADOS
    // =====================================================

    boolean existsByDespachoDetalleId(
            Long despachoDetalleId
    );

}
