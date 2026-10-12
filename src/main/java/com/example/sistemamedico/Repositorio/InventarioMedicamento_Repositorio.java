package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.InventarioMedicamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InventarioMedicamento_Repositorio
        extends JpaRepository<InventarioMedicamento, Long> {


    // =====================================================
    // BUSCAR INVENTARIO DE UN MEDICAMENTO
    // EN UNA SUCURSAL
    // =====================================================

    @EntityGraph(attributePaths = "medicamento")
    Optional<InventarioMedicamento>
    findByMedicamentoIdAndSucursalIdAndActivoTrue(
            Long medicamentoId,
            Long sucursalId
    );


    // =====================================================
    // LISTAR INVENTARIO ACTIVO DE UNA SUCURSAL
    // =====================================================

    @EntityGraph(attributePaths = "medicamento")
    List<InventarioMedicamento>
    findBySucursalIdAndActivoTrueOrderByMedicamentoNombreAsc(
            Long sucursalId
    );
}
