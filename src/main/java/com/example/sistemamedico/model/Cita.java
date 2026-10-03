package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "citas")
public class Cita {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // PACIENTE
    // =====================================================

    @ManyToOne
    @JoinColumn(
            name = "paciente_id",
            nullable = false
    )
    private Usuario paciente;


    // =====================================================
    // MÉDICO
    // =====================================================

    @ManyToOne
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Usuario medico;


    // =====================================================
    // SUCURSAL
    // =====================================================

    @ManyToOne
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;


    // =====================================================
    // ESPECIALIDAD
    // =====================================================

    @ManyToOne
    @JoinColumn(
            name = "especialidad_id",
            nullable = false
    )
    private Especialidad especialidad;


    // =====================================================
    // HORARIO
    // =====================================================

    @ManyToOne
    @JoinColumn(
            name = "horario_id",
            nullable = false
    )
    private HorarioMedico horario;


    // =====================================================
    // MOTIVO DE CONSULTA
    // =====================================================

    @Column(
            name = "motivo_consulta",
            nullable = false,
            length = 2000
    )
    private String motivoConsulta;


    // =====================================================
    // ESTADO
    // =====================================================

    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado;


    // =====================================================
    // PRIORIDAD
    // CU-05
    // =====================================================

    @Column(
            name = "prioridad",
            length = 20
    )
    private String prioridad;


    // =====================================================
    // HORA DE LLEGADA
    // CU-05
    // =====================================================

    @Column(
            name = "hora_llegada"
    )
    private LocalDateTime horaLlegada;


    // =====================================================
    // NOTA DE REASIGNACIÓN
    // CU-05 FA07
    // =====================================================

    @Column(
            name = "nota_reasignacion",
            length = 1000
    )
    private String notaReasignacion;


    // =====================================================
    // ORIGEN DE LA CITA
    // CU-06 FA03
    //
    // PORTAL  = creada por el paciente
    // INTERNO = creada por personal interno
    // =====================================================

    @Column(
            name = "origen",
            length = 20
    )
    private String origen;


    // =====================================================
    // FECHA DE CREACIÓN
    // =====================================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    public void prePersist() {

        // =================================================
        // FECHA DE CREACIÓN
        // =================================================

        if (fechaCreacion == null) {

            fechaCreacion =
                    LocalDateTime.now();
        }


        // =================================================
        // ESTADO INICIAL
        // =================================================

        if (
                estado == null
                        ||
                        estado.isBlank()
        ) {

            estado =
                    "PENDIENTE_PAGO";
        }


        // =================================================
        // PRIORIDAD INICIAL
        // =================================================

        if (
                prioridad == null
                        ||
                        prioridad.isBlank()
        ) {

            prioridad =
                    "NORMAL";
        }


        // =================================================
        // ORIGEN INICIAL
        //
        // Por defecto las citas se consideran creadas
        // desde el portal.
        // Las citas Walk-in se cambiarán explícitamente
        // a INTERNO desde el flujo de recepción.
        // =================================================

        if (
                origen == null
                        ||
                        origen.isBlank()
        ) {

            origen =
                    "PORTAL";
        }
    }


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public Cita() {
    }


    // =====================================================
    // GETTERS Y SETTERS
    // =====================================================

    public Long getId() {

        return id;
    }


    public void setId(
            Long id
    ) {

        this.id =
                id;
    }


    public Usuario getPaciente() {

        return paciente;
    }


    public void setPaciente(
            Usuario paciente
    ) {

        this.paciente =
                paciente;
    }


    public Usuario getMedico() {

        return medico;
    }


    public void setMedico(
            Usuario medico
    ) {

        this.medico =
                medico;
    }


    public Sucursal getSucursal() {

        return sucursal;
    }


    public void setSucursal(
            Sucursal sucursal
    ) {

        this.sucursal =
                sucursal;
    }


    public Especialidad getEspecialidad() {

        return especialidad;
    }


    public void setEspecialidad(
            Especialidad especialidad
    ) {

        this.especialidad =
                especialidad;
    }


    public HorarioMedico getHorario() {

        return horario;
    }


    public void setHorario(
            HorarioMedico horario
    ) {

        this.horario =
                horario;
    }


    public String getMotivoConsulta() {

        return motivoConsulta;
    }


    public void setMotivoConsulta(
            String motivoConsulta
    ) {

        this.motivoConsulta =
                motivoConsulta;
    }


    public String getEstado() {

        return estado;
    }


    public void setEstado(
            String estado
    ) {

        this.estado =
                estado;
    }


    public String getPrioridad() {

        return prioridad;
    }


    public void setPrioridad(
            String prioridad
    ) {

        this.prioridad =
                prioridad;
    }


    public LocalDateTime getHoraLlegada() {

        return horaLlegada;
    }


    public void setHoraLlegada(
            LocalDateTime horaLlegada
    ) {

        this.horaLlegada =
                horaLlegada;
    }


    public String getNotaReasignacion() {

        return notaReasignacion;
    }


    public void setNotaReasignacion(
            String notaReasignacion
    ) {

        this.notaReasignacion =
                notaReasignacion;
    }


    public String getOrigen() {

        return origen;
    }


    public void setOrigen(
            String origen
    ) {

        this.origen =
                origen;
    }


    public LocalDateTime getFechaCreacion() {

        return fechaCreacion;
    }


    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {

        this.fechaCreacion =
                fechaCreacion;
    }
}
