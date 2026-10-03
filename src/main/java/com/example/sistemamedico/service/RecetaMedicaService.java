package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.Medicamento_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedicaDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedica_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Medicamento;
import com.example.sistemamedico.model.RecetaMedica;
import com.example.sistemamedico.model.RecetaMedicaDetalle;
import com.example.sistemamedico.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class RecetaMedicaService {


    private final Cita_Repositorio citaRepository;

    private final Medicamento_Repositorio
            medicamentoRepository;

    private final RecetaMedica_Repositorio
            recetaMedicaRepository;

    private final RecetaMedicaDetalle_Repositorio
            recetaMedicaDetalleRepository;


    public RecetaMedicaService(

            Cita_Repositorio citaRepository,

            Medicamento_Repositorio
                    medicamentoRepository,

            RecetaMedica_Repositorio
                    recetaMedicaRepository,

            RecetaMedicaDetalle_Repositorio
                    recetaMedicaDetalleRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.medicamentoRepository =
                medicamentoRepository;

        this.recetaMedicaRepository =
                recetaMedicaRepository;

        this.recetaMedicaDetalleRepository =
                recetaMedicaDetalleRepository;
    }


    // =====================================================
    // OBTENER MEDICAMENTOS ACTIVOS
    // =====================================================

    public List<Medicamento>
    obtenerMedicamentosActivos() {

        return medicamentoRepository
                .findByActivoTrueOrderByNombreAsc();
    }


    // =====================================================
    // OBTENER CITA
    // =====================================================

    public Cita obtenerCita(
            Long citaId
    ) {

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
    // OBTENER RECETAS DE UNA CITA
    // =====================================================

    public List<RecetaMedica>
    obtenerRecetasPorCita(
            Long citaId
    ) {

        return recetaMedicaRepository
                .findByCitaIdOrderByFechaCreacionDesc(
                        citaId
                );
    }


    // =====================================================
    // OBTENER DETALLES DE UNA RECETA
    // =====================================================

    public List<RecetaMedicaDetalle>
    obtenerDetallesReceta(
            Long recetaId
    ) {

        return recetaMedicaDetalleRepository
                .findByRecetaIdOrderByIdAsc(
                        recetaId
                );
    }


    // =====================================================
    // GENERAR RECETA MEDICA
    // FA04 - CU08
    // =====================================================

    @Transactional
    public RecetaMedica generarReceta(

            Long citaId,

            Usuario medico,

            List<Long> medicamentoIds,

            List<String> dosis,

            List<String> frecuencias,

            List<String> duraciones,

            List<String> indicaciones
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
        // VALIDAR MEDICO ASIGNADO
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
                    "La receta médica solo puede generarse "
                            + "cuando la consulta está evaluada "
                            + "y pendiente de cierre."
            );
        }


        // =============================================
        // VALIDAR MEDICAMENTOS
        // =============================================

        if (
                medicamentoIds == null
                        ||
                        medicamentoIds.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un medicamento."
            );
        }


        // =============================================
        // VALIDAR CANTIDAD DE DATOS
        // =============================================

        int cantidad =
                medicamentoIds.size();


        if (
                dosis == null
                        ||
                        frecuencias == null
                        ||
                        duraciones == null
                        ||
                        indicaciones == null
                        ||
                        dosis.size() != cantidad
                        ||
                        frecuencias.size() != cantidad
                        ||
                        duraciones.size() != cantidad
                        ||
                        indicaciones.size() != cantidad
        ) {

            throw new IllegalArgumentException(
                    "Los datos de los medicamentos están incompletos."
            );
        }


        // =============================================
        // CREAR RECETA
        // =============================================

        RecetaMedica receta =
                new RecetaMedica();


        receta.setCita(
                cita
        );

        receta.setMedico(
                medico
        );


        RecetaMedica recetaGuardada =
                recetaMedicaRepository
                        .save(
                                receta
                        );


        // =============================================
        // CREAR DETALLES
        // =============================================

        List<RecetaMedicaDetalle> detalles =
                new ArrayList<>();


        for (
                int i = 0;
                i < cantidad;
                i++
        ) {


            Long medicamentoId =
                    medicamentoIds.get(i);


            // =========================================
            // VALIDAR ID
            // =========================================

            if (medicamentoId == null) {

                throw new IllegalArgumentException(
                        "Existe un medicamento inválido."
                );
            }


            // =========================================
            // BUSCAR MEDICAMENTO
            // =========================================

            Medicamento medicamento =
                    medicamentoRepository
                            .findById(
                                    medicamentoId
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Uno de los medicamentos "
                                                            + "seleccionados no existe."
                                            )
                            );


            // =========================================
            // VALIDAR MEDICAMENTO ACTIVO
            // =========================================

            if (
                    medicamento.getActivo() == null
                            ||
                            !medicamento.getActivo()
            ) {

                throw new IllegalArgumentException(
                        "El medicamento "
                                + medicamento.getNombre()
                                + " no está disponible."
                );
            }


            // =========================================
            // VALIDAR DOSIS
            // =========================================

            String dosisLimpia =
                    limpiarObligatorio(
                            dosis.get(i),
                            "La dosis es obligatoria."
                    );


            // =========================================
            // VALIDAR FRECUENCIA
            // =========================================

            String frecuenciaLimpia =
                    limpiarObligatorio(
                            frecuencias.get(i),
                            "La frecuencia es obligatoria."
                    );


            // =========================================
            // VALIDAR DURACION
            // =========================================

            String duracionLimpia =
                    limpiarObligatorio(
                            duraciones.get(i),
                            "La duración es obligatoria."
                    );


            // =========================================
            // INDICACIONES
            // =========================================

            String indicacionLimpia =
                    limpiarOpcional(
                            indicaciones.get(i)
                    );


            // =========================================
            // CREAR DETALLE
            // =========================================

            RecetaMedicaDetalle detalle =
                    new RecetaMedicaDetalle();


            detalle.setReceta(
                    recetaGuardada
            );

            detalle.setMedicamento(
                    medicamento
            );

            detalle.setDosis(
                    dosisLimpia
            );

            detalle.setFrecuencia(
                    frecuenciaLimpia
            );

            detalle.setDuracion(
                    duracionLimpia
            );

            detalle.setIndicaciones(
                    indicacionLimpia
            );


            detalles.add(
                    detalle
            );
        }


        // =============================================
        // GUARDAR DETALLES
        // =============================================

        recetaMedicaDetalleRepository
                .saveAll(
                        detalles
                );


        return recetaGuardada;
    }


    // =====================================================
    // LIMPIAR CAMPO OBLIGATORIO
    // =====================================================

    private String limpiarObligatorio(

            String texto,

            String mensajeError
    ) {

        if (
                texto == null
                        ||
                        texto.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    mensajeError
            );
        }


        return texto.trim();
    }


    // =====================================================
    // LIMPIAR CAMPO OPCIONAL
    // =====================================================

    private String limpiarOpcional(
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