package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.ExamenLaboratorio_Repositorio;
import com.example.sistemamedico.Repositorio.OrdenLaboratorioDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.OrdenLaboratorio_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.ExamenLaboratorio;
import com.example.sistemamedico.model.OrdenLaboratorio;
import com.example.sistemamedico.model.OrdenLaboratorioDetalle;
import com.example.sistemamedico.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrdenLaboratorioService {

    private final Cita_Repositorio citaRepository;

    private final ExamenLaboratorio_Repositorio
            examenLaboratorioRepository;

    private final OrdenLaboratorio_Repositorio
            ordenLaboratorioRepository;

    private final OrdenLaboratorioDetalle_Repositorio
            ordenLaboratorioDetalleRepository;


    public OrdenLaboratorioService(

            Cita_Repositorio citaRepository,

            ExamenLaboratorio_Repositorio
                    examenLaboratorioRepository,

            OrdenLaboratorio_Repositorio
                    ordenLaboratorioRepository,

            OrdenLaboratorioDetalle_Repositorio
                    ordenLaboratorioDetalleRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.examenLaboratorioRepository =
                examenLaboratorioRepository;

        this.ordenLaboratorioRepository =
                ordenLaboratorioRepository;

        this.ordenLaboratorioDetalleRepository =
                ordenLaboratorioDetalleRepository;
    }


    // =====================================================
    // OBTENER CATALOGO DE EXAMENES ACTIVOS
    // =====================================================

    public List<ExamenLaboratorio>
    obtenerExamenesActivos() {

        return examenLaboratorioRepository
                .findByActivoTrueOrderByNombreAsc();
    }


    // =====================================================
    // OBTENER CITA
    // =====================================================

    public Cita obtenerCita(Long citaId) {

        return citaRepository
                .findById(citaId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "La cita no existe."
                                )
                );
    }


    // =====================================================
    // OBTENER ORDENES DE UNA CITA
    // =====================================================

    public List<OrdenLaboratorio>
    obtenerOrdenesPorCita(Long citaId) {

        return ordenLaboratorioRepository
                .findByCitaIdOrderByFechaCreacionDesc(
                        citaId
                );
    }


    // =====================================================
    // OBTENER DETALLES DE UNA ORDEN
    // =====================================================

    public List<OrdenLaboratorioDetalle>
    obtenerDetallesOrden(Long ordenId) {

        return ordenLaboratorioDetalleRepository
                .findByOrdenIdOrderByIdAsc(
                        ordenId
                );
    }


    // =====================================================
    // GENERAR ORDEN DE LABORATORIO
    // CU-08 + PREPARACION CU-09
    // =====================================================

    @Transactional
    public OrdenLaboratorio generarOrden(

            Long citaId,

            Usuario medico,

            List<Long> examenIds,

            String observaciones,

            Boolean ordenExterna
    ) {


        // =============================================
        // VALIDAR MEDICO
        // =============================================

        if (
                medico == null
                        ||
                        medico.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "No se encontró el médico autenticado."
            );
        }


        // =============================================
        // BUSCAR CITA
        // =============================================

        Cita cita =
                citaRepository
                        .findById(citaId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La cita no existe."
                                        )
                        );


        // =============================================
        // VALIDAR MEDICO ASIGNADO A LA CITA
        // =============================================

        if (
                cita.getMedico() == null
                        ||
                        cita.getMedico().getId() == null
                        ||
                        !cita.getMedico()
                                .getId()
                                .equals(
                                        medico.getId()
                                )
        ) {

            throw new IllegalArgumentException(
                    "La cita no pertenece al médico autenticado."
            );
        }


        // =============================================
        // VALIDAR ESTADO DE LA CITA
        // =============================================

        if (
                cita.getEstado() == null
                        ||
                        !"EVALUADO_PENDIENTE_CIERRE"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            throw new IllegalArgumentException(
                    "La orden de laboratorio solo puede generarse "
                            + "cuando la consulta está evaluada "
                            + "y pendiente de cierre."
            );
        }


        // =============================================
        // VALIDAR EXAMENES
        // =============================================

        if (
                examenIds == null
                        ||
                        examenIds.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un examen de laboratorio."
            );
        }


        // =============================================
        // ELIMINAR IDS REPETIDOS
        // =============================================

        Set<Long> idsUnicos =
                new LinkedHashSet<>();


        for (Long id : examenIds) {

            if (id != null) {
                idsUnicos.add(id);
            }
        }


        if (idsUnicos.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un examen de laboratorio."
            );
        }


        // =============================================
        // BUSCAR EXAMENES
        // =============================================

        List<ExamenLaboratorio> examenes =
                examenLaboratorioRepository
                        .findAllById(
                                idsUnicos
                        );


        // =============================================
        // VALIDAR QUE TODOS EXISTAN
        // =============================================

        if (
                examenes.size()
                        !=
                        idsUnicos.size()
        ) {

            throw new IllegalArgumentException(
                    "Uno o más exámenes seleccionados no existen."
            );
        }


        // =============================================
        // VALIDAR EXAMENES ACTIVOS
        // Y CALCULAR MONTO TOTAL
        // =============================================

        BigDecimal montoTotal =
                BigDecimal.ZERO;


        for (
                ExamenLaboratorio examen
                :
                examenes
        ) {

            if (
                    examen.getActivo() == null
                            ||
                            !examen.getActivo()
            ) {

                throw new IllegalArgumentException(
                        "El examen "
                                + examen.getNombre()
                                + " no está disponible."
                );
            }


            BigDecimal precio =
                    examen.getPrecio();


            if (precio == null) {

                precio =
                        BigDecimal.ZERO;
            }


            montoTotal =
                    montoTotal.add(
                            precio
                    );
        }


        // =============================================
        // CREAR ORDEN
        // =============================================

        OrdenLaboratorio orden =
                new OrdenLaboratorio();


        orden.setCita(
                cita
        );

        orden.setMedico(
                medico
        );

        orden.setObservaciones(
                limpiarTexto(
                        observaciones
                )
        );


        // =============================================
        // ESTADO INICIAL CU-09
        // =============================================

        orden.setEstado(
                "PENDIENTE"
        );


        // =============================================
        // MONTO TOTAL
        // =============================================

        orden.setMontoTotal(
                montoTotal
        );


        // =============================================
        // ORDEN EXTERNA
        // =============================================

        orden.setOrdenExterna(
                Boolean.TRUE.equals(
                        ordenExterna
                )
        );


        // =============================================
        // GUARDAR ORDEN
        // =============================================

        OrdenLaboratorio ordenGuardada =
                ordenLaboratorioRepository
                        .save(
                                orden
                        );


        // =============================================
        // CREAR DETALLES
        // =============================================

        List<OrdenLaboratorioDetalle> detalles =
                new ArrayList<>();


        for (
                ExamenLaboratorio examen
                :
                examenes
        ) {

            OrdenLaboratorioDetalle detalle =
                    new OrdenLaboratorioDetalle();


            detalle.setOrden(
                    ordenGuardada
            );

            detalle.setExamen(
                    examen
            );


            // =========================================
            // GUARDAR PRECIO DEL EXAMEN
            // =========================================

            BigDecimal precio =
                    examen.getPrecio();


            if (precio == null) {

                precio =
                        BigDecimal.ZERO;
            }


            detalle.setMonto(
                    precio
            );


            // =========================================
            // CAMPOS DE RESULTADO
            // AUN VACIOS
            // =========================================

            detalle.setValorResultado(
                    null
            );

            detalle.setUnidad(
                    null
            );

            detalle.setFechaResultado(
                    null
            );

            detalle.setFueraRango(
                    false
            );

            detalle.setNotasResultado(
                    null
            );

            detalle.setPublicado(
                    false
            );


            detalles.add(
                    detalle
            );
        }


        // =============================================
        // GUARDAR DETALLES
        // =============================================

        ordenLaboratorioDetalleRepository
                .saveAll(
                        detalles
                );


        return ordenGuardada;
    }


    // =====================================================
    // LIMPIAR OBSERVACIONES
    // =====================================================

    private String limpiarTexto(
            String texto
    ) {

        if (texto == null) {
            return null;
        }


        String limpio =
                texto.trim();


        if (limpio.isEmpty()) {
            return null;
        }


        return limpio;
    }
}