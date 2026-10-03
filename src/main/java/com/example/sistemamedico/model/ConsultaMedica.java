package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consulta_medica")
public class ConsultaMedica {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA
    // ==========================================

    @OneToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false,
            unique = true
    )
    private Cita cita;


    // ==========================================
    // MEDICO
    // ==========================================

    @ManyToOne
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Usuario medico;


    // ==========================================
    // INFORMACION DE LA CONSULTA
    // ==========================================

    @Column(
            name = "motivo_visita",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String motivoVisita;


    @Column(
            name = "hallazgos_clinicos",
            columnDefinition = "TEXT"
    )
    private String hallazgosClinicos;


    @Column(
            name = "codigo_cie10",
            length = 20
    )
    private String codigoCie10;


    @Column(
            name = "diagnostico",
            columnDefinition = "TEXT"
    )
    private String diagnostico;


    @Column(
            name = "plan_tratamiento",
            columnDefinition = "TEXT"
    )
    private String planTratamiento;


    @Column(
            name = "notas_adicionales",
            columnDefinition = "TEXT"
    )
    private String notasAdicionales;


    // ==========================================
    // ESTADO
    // ==========================================

    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado;


    // ==========================================
    // FECHAS
    // ==========================================

    @Column(
            name = "fecha_inicio",
            nullable = false
    )
    private LocalDateTime fechaInicio;


    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;


    // ==========================================
    // VALORES POR DEFECTO
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (estado == null) {
            estado = "EN_CURSO";
        }

        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }


    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }


    public Usuario getMedico() {
        return medico;
    }

    public void setMedico(Usuario medico) {
        this.medico = medico;
    }


    public String getMotivoVisita() {
        return motivoVisita;
    }

    public void setMotivoVisita(String motivoVisita) {
        this.motivoVisita = motivoVisita;
    }


    public String getHallazgosClinicos() {
        return hallazgosClinicos;
    }

    public void setHallazgosClinicos(String hallazgosClinicos) {
        this.hallazgosClinicos = hallazgosClinicos;
    }


    public String getCodigoCie10() {
        return codigoCie10;
    }

    public void setCodigoCie10(String codigoCie10) {
        this.codigoCie10 = codigoCie10;
    }


    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }


    public String getPlanTratamiento() {
        return planTratamiento;
    }

    public void setPlanTratamiento(String planTratamiento) {
        this.planTratamiento = planTratamiento;
    }


    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }


    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(
            LocalDateTime fechaFinalizacion
    ) {
        this.fechaFinalizacion =
                fechaFinalizacion;
    }
}
