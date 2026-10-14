
package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.AgendaMedicaEvento;
import com.example.sistemamedico.model.AgendaMedicaTarea;

import com.example.sistemamedico.service.AgendaMedicaService;
import com.example.sistemamedico.service.AgendaMedicaSeguridadService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medico/agenda/api")
public class AgendaMedicaController {

    // =====================================================
    // CU-16: DEPENDENCIAS
    // =====================================================

    private final AgendaMedicaService agendaService;

    private final AgendaMedicaSeguridadService seguridadService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AgendaMedicaController(
            AgendaMedicaService agendaService,
            AgendaMedicaSeguridadService seguridadService
    ) {

        this.agendaService = agendaService;

        this.seguridadService = seguridadService;
    }

    // =====================================================
    // CU-16: CONSULTAR TODOS LOS EVENTOS
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/eventos")
    public List<AgendaMedicaEvento> listarEventos(
            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.listarEventos(medicoId);
    }

    // =====================================================
    // CU-16: CONSULTAR EVENTOS POR PERÍODO
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/eventos/periodo")
    public List<AgendaMedicaEvento> eventosPeriodo(

            @RequestParam LocalDateTime inicio,

            @RequestParam LocalDateTime fin,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.eventosEnPeriodo(
                medicoId,
                inicio,
                fin
        );
    }

    // =====================================================
    // CU-16: CONSULTAR DETALLE DE UN EVENTO
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/eventos/{id}")
    public AgendaMedicaEvento obtenerEvento(

            @PathVariable Long id,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.obtenerEvento(
                id,
                medicoId
        );
    }

    // =====================================================
    // CU-16: CREAR EVENTO
    // PERMISO: medical-agenda/create
    // =====================================================

    @PostMapping("/eventos")
    public AgendaMedicaEvento crearEvento(

            @RequestBody EventoSolicitud datos,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "create"
        );

        return agendaService.crearEvento(
                medicoId,
                datos.titulo(),
                datos.descripcion(),
                datos.fechaInicio(),
                datos.fechaFin(),
                datos.tipoEvento(),
                datos.todoElDia()
        );
    }

    // =====================================================
    // CU-16: ACTUALIZAR EVENTO
    // PERMISO: medical-agenda/update
    // =====================================================

    @PutMapping("/eventos/{id}")
    public AgendaMedicaEvento actualizarEvento(

            @PathVariable Long id,

            @RequestBody EventoSolicitud datos,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "update"
        );

        return agendaService.actualizarEvento(
                id,
                medicoId,
                datos.titulo(),
                datos.descripcion(),
                datos.fechaInicio(),
                datos.fechaFin(),
                datos.tipoEvento(),
                datos.todoElDia()
        );
    }

    // =====================================================
    // CU-16: ELIMINAR EVENTO
    // PERMISO: medical-agenda/delete
    // =====================================================

    @DeleteMapping("/eventos/{id}")
    public ResponseEntity<Void> eliminarEvento(

            @PathVariable Long id,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "delete"
        );

        agendaService.eliminarEvento(
                id,
                medicoId
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // CU-16: CONSULTAR Y FILTRAR TAREAS
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/tareas")
    public List<AgendaMedicaTarea> listarTareas(

            @RequestParam(required = false)
            Boolean completadas,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.filtrarTareas(
                medicoId,
                completadas
        );
    }

    // =====================================================
    // CU-16: CONSULTAR TAREAS DE UNA FECHA
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/tareas/fecha")
    public List<AgendaMedicaTarea> tareasFecha(

            @RequestParam LocalDate fecha,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.tareasDeFecha(
                medicoId,
                fecha
        );
    }

    // =====================================================
    // CU-16: CONSULTAR DETALLE DE UNA TAREA
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/tareas/{id}")
    public AgendaMedicaTarea obtenerTarea(

            @PathVariable Long id,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "read"
        );

        return agendaService.obtenerTarea(
                id,
                medicoId
        );
    }

    // =====================================================
    // CU-16: CREAR TAREA
    // PERMISO: medical-agenda/create
    // =====================================================

    @PostMapping("/tareas")
    public AgendaMedicaTarea crearTarea(

            @RequestBody TareaSolicitud datos,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "create"
        );

        return agendaService.crearTarea(
                medicoId,
                datos.titulo(),
                datos.descripcion(),
                datos.prioridad(),
                datos.fechaLimite()
        );
    }

    // =====================================================
    // CU-16: ACTUALIZAR TAREA
    // PERMISO: medical-agenda/update
    // =====================================================

    @PutMapping("/tareas/{id}")
    public AgendaMedicaTarea actualizarTarea(

            @PathVariable Long id,

            @RequestBody TareaSolicitud datos,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "update"
        );

        return agendaService.actualizarTarea(
                id,
                medicoId,
                datos.titulo(),
                datos.descripcion(),
                datos.prioridad(),
                datos.fechaLimite()
        );
    }

    // =====================================================
    // CU-16: COMPLETAR O VOLVER A PENDIENTE
    // PERMISO: medical-agenda/update
    // =====================================================

    @PatchMapping("/tareas/{id}/estado")
    public AgendaMedicaTarea cambiarEstadoTarea(

            @PathVariable Long id,

            @RequestBody EstadoTareaSolicitud datos,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "update"
        );

        if (datos.isCompleted() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar el estado de la tarea."
            );
        }

        return agendaService.cambiarEstadoTarea(
                id,
                medicoId,
                datos.isCompleted()
        );
    }

    // =====================================================
    // CU-16: ELIMINAR TAREA
    // PERMISO: medical-agenda/delete
    // =====================================================

    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<Void> eliminarTarea(

            @PathVariable Long id,

            HttpSession session
    ) {

        Long medicoId = seguridadService.obtenerMedicoId(
                session,
                "delete"
        );

        agendaService.eliminarTarea(
                id,
                medicoId
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // CU-16: MANEJAR ERRORES DE VALIDACIÓN
    //
    // HTTP 400: DATOS INVÁLIDOS
    // HTTP 404: REGISTRO NO ENCONTRADO
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> errorValidacion(
            IllegalArgumentException ex
    ) {

        String mensaje = ex.getMessage() == null
                ? "Solicitud inválida."
                : ex.getMessage();

        HttpStatus estado;

        if (
                mensaje.contains("no encontrado")
                        ||
                        mensaje.contains("sin autorización")
        ) {

            estado = HttpStatus.NOT_FOUND;

        } else {

            estado = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(estado)
                .body(
                        Map.of(
                                "error",
                                mensaje
                        )
                );
    }

    // =====================================================
    // CU-16: MANEJAR CONFLICTOS DE AGENDA
    //
    // CORRECCIÓN DEL PASO 335
    //
    // HTTP 409 - CONFLICT
    //
    // EJEMPLO:
    // EL MÉDICO INTENTA CREAR UN EVENTO
    // SOBRE UNA CITA YA CONFIRMADA.
    // =====================================================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> errorConflictoAgenda(
            IllegalStateException ex
    ) {

        String mensaje = ex.getMessage() == null
                ? "No se puede realizar la operación "
                + "debido a un conflicto de agenda."
                : ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "error",
                                mensaje
                        )
                );
    }

    // =====================================================
    // CU-16: DATOS PARA CREAR O EDITAR EVENTOS
    // =====================================================

    public record EventoSolicitud(

            String titulo,

            String descripcion,

            LocalDateTime fechaInicio,

            LocalDateTime fechaFin,

            Integer tipoEvento,

            Boolean todoElDia

    ) {
    }

    // =====================================================
    // CU-16: DATOS PARA CREAR O EDITAR TAREAS
    // =====================================================

    public record TareaSolicitud(

            String titulo,

            String descripcion,

            Integer prioridad,

            LocalDateTime fechaLimite

    ) {
    }

    // =====================================================
    // CU-16: DATOS PARA CAMBIAR ESTADO DE TAREA
    // =====================================================

    public record EstadoTareaSolicitud(

            Boolean isCompleted

    ) {
    }
}
