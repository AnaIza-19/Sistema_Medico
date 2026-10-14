package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AgendaMedicaEvento_Repositorio;
import com.example.sistemamedico.Repositorio.AgendaMedicaTarea_Repositorio;

import com.example.sistemamedico.model.AgendaMedicaEvento;
import com.example.sistemamedico.model.AgendaMedicaTarea;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendaMedicaService {

    private final AgendaMedicaEvento_Repositorio eventoRepositorio;
    private final AgendaMedicaTarea_Repositorio tareaRepositorio;
    private final AgendaMedicaConflictoCitasService conflictoCitasService;
    private final AgendaMedicaConcurrenciaService agendaMedicaConcurrenciaService;

    public AgendaMedicaService(
            AgendaMedicaEvento_Repositorio eventoRepositorio,
            AgendaMedicaTarea_Repositorio tareaRepositorio,
            AgendaMedicaConflictoCitasService conflictoCitasService,
            AgendaMedicaConcurrenciaService agendaMedicaConcurrenciaService
    ) {
        this.eventoRepositorio = eventoRepositorio;
        this.tareaRepositorio = tareaRepositorio;
        this.conflictoCitasService = conflictoCitasService;
        this.agendaMedicaConcurrenciaService = agendaMedicaConcurrenciaService;
    }

    // =====================================================
    // VALIDACIONES GENERALES
    // =====================================================

    private void validarMedico(Long medicoId) {
        if (medicoId == null || medicoId <= 0) {
            throw new IllegalArgumentException(
                    "No se identificó al médico."
            );
        }
    }

    private String validarTitulo(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El título es obligatorio."
            );
        }

        String limpio = titulo.trim();

        if (limpio.length() > 150) {
            throw new IllegalArgumentException(
                    "El título no debe superar 150 caracteres."
            );
        }

        return limpio;
    }

    private String validarDescripcion(
            String descripcion,
            int maximo
    ) {

        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        String limpia = descripcion.trim();

        if (limpia.length() > maximo) {
            throw new IllegalArgumentException(
                    "La descripción no debe superar "
                            + maximo + " caracteres."
            );
        }

        return limpia;
    }

    private void validarFechasEvento(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "Las fechas de inicio y fin son obligatorias."
            );
        }

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior "
                            + "a la fecha de inicio."
            );
        }
    }

    // =====================================================
    // EVENTOS: CONSULTAR
    // =====================================================

    @Transactional(readOnly = true)
    public List<AgendaMedicaEvento> listarEventos(
            Long medicoId
    ) {

        validarMedico(medicoId);

        return eventoRepositorio
                .findByMedicoIdOrderByFechaInicioAsc(medicoId);
    }

    @Transactional(readOnly = true)
    public List<AgendaMedicaEvento> eventosEnPeriodo(
            Long medicoId,
            LocalDateTime inicio,
            LocalDateTime finExclusivo
    ) {

        validarMedico(medicoId);
        validarFechasEvento(inicio, finExclusivo);

        return eventoRepositorio
                .findByMedicoIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
                        medicoId,
                        finExclusivo,
                        inicio
                );
    }

    @Transactional(readOnly = true)
    public AgendaMedicaEvento obtenerEvento(
            Long id,
            Long medicoId
    ) {

        validarMedico(medicoId);

        return eventoRepositorio.findByIdAndMedicoId(
                id, medicoId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Evento no encontrado o sin autorización."
                )
        );
    }

    // =====================================================
    // EVENTOS: CREAR
    // =====================================================

    @Transactional
    public AgendaMedicaEvento crearEvento(
            Long medicoId,
            String titulo,
            String descripcion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Integer tipoEvento,
            Boolean todoElDia
    ) {

        validarMedico(medicoId);

        // CU-16: serializar cambios de agenda y reservas por médico.
        agendaMedicaConcurrenciaService.bloquearMedico(medicoId);

        String tituloValido = validarTitulo(titulo);
        String descripcionValida =
                validarDescripcion(descripcion, 500);

        if (tipoEvento == null
                || tipoEvento < 0 || tipoEvento > 4) {
            throw new IllegalArgumentException(
                    "Seleccione un tipo de evento válido."
            );
        }

        boolean diaCompleto =
                Boolean.TRUE.equals(todoElDia);

        if (diaCompleto) {
            if (fechaInicio == null || fechaFin == null) {
                throw new IllegalArgumentException(
                        "Seleccione las fechas del evento."
                );
            }

            // Las fechas de eventos de día completo
            // se almacenan como intervalo:
            // [inicio del primer día, inicio del siguiente
            // día después de la fecha final].
            LocalDate primerDia = fechaInicio.toLocalDate();
            LocalDate ultimoDia = fechaFin.toLocalDate();

            if (ultimoDia.isBefore(primerDia)) {
                throw new IllegalArgumentException(
                        "La fecha final no puede ser anterior "
                                + "a la fecha inicial."
                );
            }

            fechaInicio = primerDia.atStartOfDay();
            fechaFin = ultimoDia.plusDays(1).atStartOfDay();
        }

        validarFechasEvento(fechaInicio, fechaFin);

        // CU-16: evitar eventos que se superpongan con citas existentes.
        conflictoCitasService.validarSinConflicto(
                medicoId,
                fechaInicio,
                fechaFin
        );

        AgendaMedicaEvento evento = new AgendaMedicaEvento();

        evento.setMedicoId(medicoId);
        evento.setTitulo(tituloValido);
        evento.setDescripcion(descripcionValida);
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setTipoEvento(tipoEvento.shortValue());
        evento.setTodoElDia(diaCompleto);

        return eventoRepositorio.save(evento);
    }

    // =====================================================
    // EVENTOS: ACTUALIZAR
    // =====================================================

    @Transactional
    public AgendaMedicaEvento actualizarEvento(
            Long id,
            Long medicoId,
            String titulo,
            String descripcion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Integer tipoEvento,
            Boolean todoElDia
    ) {

        validarMedico(medicoId);

        // CU-16: bloquear antes de leer y modificar el evento.
        agendaMedicaConcurrenciaService.bloquearMedico(medicoId);

        AgendaMedicaEvento evento =
                obtenerEvento(id, medicoId);

        String tituloValido = validarTitulo(titulo);
        String descripcionValida =
                validarDescripcion(descripcion, 500);

        if (tipoEvento == null
                || tipoEvento < 0 || tipoEvento > 4) {
            throw new IllegalArgumentException(
                    "Seleccione un tipo de evento válido."
            );
        }

        boolean diaCompleto =
                Boolean.TRUE.equals(todoElDia);

        if (diaCompleto) {
            if (fechaInicio == null || fechaFin == null) {
                throw new IllegalArgumentException(
                        "Seleccione las fechas del evento."
                );
            }

            LocalDate primerDia = fechaInicio.toLocalDate();
            LocalDate ultimoDia = fechaFin.toLocalDate();

            if (ultimoDia.isBefore(primerDia)) {
                throw new IllegalArgumentException(
                        "La fecha final no puede ser anterior "
                                + "a la fecha inicial."
                );
            }

            fechaInicio = primerDia.atStartOfDay();
            fechaFin = ultimoDia.plusDays(1).atStartOfDay();
        }

        validarFechasEvento(fechaInicio, fechaFin);

        // CU-16: evitar eventos que se superpongan con citas existentes.
        conflictoCitasService.validarSinConflicto(
                medicoId,
                fechaInicio,
                fechaFin
        );

        evento.setTitulo(tituloValido);
        evento.setDescripcion(descripcionValida);
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setTipoEvento(tipoEvento.shortValue());
        evento.setTodoElDia(diaCompleto);

        return eventoRepositorio.save(evento);
    }

    // =====================================================
    // EVENTOS: ELIMINAR
    // =====================================================

    @Transactional
    public void eliminarEvento(Long id, Long medicoId) {

        AgendaMedicaEvento evento =
                obtenerEvento(id, medicoId);

        eventoRepositorio.delete(evento);
    }

    // =====================================================
    // TAREAS: CONSULTAR
    // =====================================================

    @Transactional(readOnly = true)
    public List<AgendaMedicaTarea> listarTareas(
            Long medicoId
    ) {

        validarMedico(medicoId);

        return tareaRepositorio
                .findByMedicoIdOrderByFechaLimiteAsc(medicoId);
    }

    @Transactional(readOnly = true)
    public List<AgendaMedicaTarea> filtrarTareas(
            Long medicoId,
            Boolean completadas
    ) {

        validarMedico(medicoId);

        if (completadas == null) {
            return listarTareas(medicoId);
        }

        return tareaRepositorio
                .findByMedicoIdAndIsCompletedOrderByFechaLimiteAsc(
                        medicoId,
                        completadas
                );
    }

    @Transactional(readOnly = true)
    public List<AgendaMedicaTarea> tareasDeFecha(
            Long medicoId,
            LocalDate fecha
    ) {

        validarMedico(medicoId);

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "Seleccione una fecha válida."
            );
        }

        return tareaRepositorio
                .findByMedicoIdAndFechaLimiteGreaterThanEqualAndFechaLimiteLessThanOrderByFechaLimiteAsc(
                        medicoId,
                        fecha.atStartOfDay(),
                        fecha.plusDays(1).atStartOfDay()
                );
    }

    @Transactional(readOnly = true)
    public AgendaMedicaTarea obtenerTarea(
            Long id,
            Long medicoId
    ) {

        validarMedico(medicoId);

        return tareaRepositorio.findByIdAndMedicoId(
                id, medicoId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Tarea no encontrada o sin autorización."
                )
        );
    }

    // =====================================================
    // TAREAS: CREAR
    // =====================================================

    @Transactional
    public AgendaMedicaTarea crearTarea(
            Long medicoId,
            String titulo,
            String descripcion,
            Integer prioridad,
            LocalDateTime fechaLimite
    ) {

        validarMedico(medicoId);

        String tituloValido = validarTitulo(titulo);

        String descripcionValida =
                validarDescripcion(descripcion, 1000);

        if (prioridad == null
                || prioridad < 0 || prioridad > 2) {
            throw new IllegalArgumentException(
                    "Seleccione una prioridad válida."
            );
        }

        if (fechaLimite == null) {
            throw new IllegalArgumentException(
                    "La fecha límite es obligatoria."
            );
        }

        AgendaMedicaTarea tarea = new AgendaMedicaTarea();

        tarea.setMedicoId(medicoId);
        tarea.setTitulo(tituloValido);
        tarea.setDescripcion(descripcionValida);
        tarea.setPrioridad(prioridad.shortValue());
        tarea.setFechaLimite(fechaLimite);
        tarea.setIsCompleted(false);

        return tareaRepositorio.save(tarea);
    }

    // =====================================================
    // TAREAS: ACTUALIZAR
    // =====================================================

    @Transactional
    public AgendaMedicaTarea actualizarTarea(
            Long id,
            Long medicoId,
            String titulo,
            String descripcion,
            Integer prioridad,
            LocalDateTime fechaLimite
    ) {

        AgendaMedicaTarea tarea =
                obtenerTarea(id, medicoId);

        String tituloValido = validarTitulo(titulo);

        String descripcionValida =
                validarDescripcion(descripcion, 1000);

        if (prioridad == null
                || prioridad < 0 || prioridad > 2) {
            throw new IllegalArgumentException(
                    "Seleccione una prioridad válida."
            );
        }

        if (fechaLimite == null) {
            throw new IllegalArgumentException(
                    "La fecha límite es obligatoria."
            );
        }

        tarea.setTitulo(tituloValido);
        tarea.setDescripcion(descripcionValida);
        tarea.setPrioridad(prioridad.shortValue());
        tarea.setFechaLimite(fechaLimite);

        return tareaRepositorio.save(tarea);
    }

    // =====================================================
    // TAREAS: COMPLETAR O VOLVER A PENDIENTE
    // =====================================================

    @Transactional
    public AgendaMedicaTarea cambiarEstadoTarea(
            Long id,
            Long medicoId,
            boolean completada
    ) {

        AgendaMedicaTarea tarea =
                obtenerTarea(id, medicoId);

        tarea.setIsCompleted(completada);

        return tareaRepositorio.save(tarea);
    }

    // =====================================================
    // TAREAS: ELIMINAR
    // =====================================================

    @Transactional
    public void eliminarTarea(Long id, Long medicoId) {

        AgendaMedicaTarea tarea =
                obtenerTarea(id, medicoId);

        tareaRepositorio.delete(tarea);
    }
}
