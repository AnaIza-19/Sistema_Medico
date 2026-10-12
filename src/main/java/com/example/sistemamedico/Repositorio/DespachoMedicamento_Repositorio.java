package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.DespachoMedicamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DespachoMedicamento_Repositorio
        extends JpaRepository<DespachoMedicamento, Long> {


    // =====================================================
    // BUSCAR UN DESPACHO POR ID
    // =====================================================

    @Override
    @EntityGraph(
            attributePaths = {
                    "receta",
                    "sucursal",
                    "farmacia"
            }
    )
    Optional<DespachoMedicamento> findById(
            Long id
    );


    // =====================================================
    // HISTORIAL DE DESPACHOS DE UNA RECETA
    // =====================================================

    List<DespachoMedicamento>
    findByRecetaIdOrderByFechaDespachoDesc(
            Long recetaId
    );


    // =====================================================
    // DESPACHOS DE UNA SUCURSAL
    // =====================================================

    List<DespachoMedicamento>
    findBySucursalIdOrderByFechaDespachoDesc(
            Long sucursalId
    );


    // =====================================================
    // VERIFICAR ESTADO DE UNA RECETA
    // =====================================================

    boolean existsByRecetaIdAndEstado(
            Long recetaId,
            String estado
    );


    // =====================================================
    // CU-11
    // HISTORIAL DE MEDICAMENTOS DEL PACIENTE
    //
    // SOLO DESPACHOS CONFIRMADOS
    // =====================================================

    @EntityGraph(
            attributePaths = {
                    "receta",
                    "receta.cita",
                    "receta.cita.paciente",
                    "sucursal",
                    "farmacia"
            }
    )
    @Query("""
            SELECT d
            FROM DespachoMedicamento d
            WHERE d.receta.cita.paciente.id = :pacienteId
              AND d.estado = 'CONFIRMADO'
            ORDER BY d.fechaDespacho DESC
            """)
    List<DespachoMedicamento> buscarHistorialPaciente(
            @Param("pacienteId")
            Long pacienteId
    );
}
