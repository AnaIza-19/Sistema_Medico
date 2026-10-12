package com.example.sistemamedico.dto;

import com.example.sistemamedico.model.RecetaMedica;

public class RecetaDespachoResultado {

    private RecetaMedica receta;
    private Long consultaId;
    private long diasTranscurridos;
    private boolean vigente;


    public RecetaDespachoResultado(
            RecetaMedica receta,
            Long consultaId,
            long diasTranscurridos,
            boolean vigente
    ) {

        this.receta = receta;
        this.consultaId = consultaId;
        this.diasTranscurridos = diasTranscurridos;
        this.vigente = vigente;
    }


    public RecetaMedica getReceta() {
        return receta;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public long getDiasTranscurridos() {
        return diasTranscurridos;
    }

    public boolean isVigente() {
        return vigente;
    }
}