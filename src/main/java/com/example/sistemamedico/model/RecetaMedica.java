package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "receta_medica")
public class RecetaMedica {


    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // CITA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    // ==========================================
    // MEDICO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Usuario medico;


    // ==========================================
    // FECHA DE CREACIÓN
    // ==========================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // ==========================================
    // RECETA ACTIVA
    // CU-11
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;


    // ==========================================
    // NOTAS
    // CU-11
    // ==========================================

    @Column(
            name = "notas",
            columnDefinition = "TEXT"
    )
    private String notas;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public RecetaMedica() {
    }


    // ==========================================
    // VALORES POR DEFECTO
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {

            fechaCreacion =
                    LocalDateTime.now();
        }


        if (activo == null) {

            activo = true;
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


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }


    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {

        this.fechaCreacion =
                fechaCreacion;
    }


    public Boolean getActivo() {
        return activo;
    }


    public void setActivo(Boolean activo) {
        this.activo = activo;
    }


    public String getNotas() {
        return notas;
    }


    public void setNotas(String notas) {
        this.notas = notas;
    }
}