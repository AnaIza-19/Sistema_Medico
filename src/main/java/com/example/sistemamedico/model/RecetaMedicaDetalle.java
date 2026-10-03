package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "receta_medica_detalle")
public class RecetaMedicaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // RECETA
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receta_id",
            nullable = false
    )
    private RecetaMedica receta;


    // ==========================================
    // MEDICAMENTO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_id",
            nullable = false
    )
    private Medicamento medicamento;


    // ==========================================
    // DOSIS
    // ==========================================

    @Column(
            name = "dosis",
            nullable = false,
            length = 100
    )
    private String dosis;


    // ==========================================
    // FRECUENCIA
    // ==========================================

    @Column(
            name = "frecuencia",
            nullable = false,
            length = 100
    )
    private String frecuencia;


    // ==========================================
    // DURACION
    // ==========================================

    @Column(
            name = "duracion",
            nullable = false,
            length = 100
    )
    private String duracion;


    // ==========================================
    // INDICACIONES
    // ==========================================

    @Column(
            name = "indicaciones",
            columnDefinition = "TEXT"
    )
    private String indicaciones;


    public RecetaMedicaDetalle() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public RecetaMedica getReceta() {
        return receta;
    }

    public void setReceta(
            RecetaMedica receta
    ) {
        this.receta = receta;
    }


    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(
            Medicamento medicamento
    ) {
        this.medicamento = medicamento;
    }


    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }


    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(
            String frecuencia
    ) {
        this.frecuencia = frecuencia;
    }


    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(
            String duracion
    ) {
        this.duracion = duracion;
    }


    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(
            String indicaciones
    ) {
        this.indicaciones = indicaciones;
    }
}
