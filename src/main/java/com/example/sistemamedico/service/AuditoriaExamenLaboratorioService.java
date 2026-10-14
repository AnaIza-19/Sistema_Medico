package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaExamenLaboratorio_Repositorio;
import com.example.sistemamedico.model.AuditoriaExamenLaboratorio;
import com.example.sistemamedico.model.ExamenLaboratorio;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaExamenLaboratorioService {
    private final AuditoriaExamenLaboratorio_Repositorio repositorio;

    public AuditoriaExamenLaboratorioService(
            AuditoriaExamenLaboratorio_Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String capturarDatos(ExamenLaboratorio examen) {
        return "ID: " + examen.getId()
                + " | Nombre: " + examen.getNombre()
                + " | Precio: " + examen.getPrecio()
                + " | Descripción: " + examen.getDescripcion()
                + " | Rango: " + examen.getRangoReferencia()
                + " | Unidad: " + examen.getUnidad()
                + " | Laboratorio ID: "
                + (examen.getLaboratorio() == null ? "Sin asignar" : examen.getLaboratorio().getId())
                + " | Activo: " + examen.getActivo();
    }

    public void registrar(String accion, ExamenLaboratorio examen,
                          String actor, String datosAnteriores, String detalle) {
        AuditoriaExamenLaboratorio auditoria = new AuditoriaExamenLaboratorio();
        auditoria.setExamenId(examen.getId());
        auditoria.setAccion(accion);
        auditoria.setNombreExamen(examen.getNombre());
        auditoria.setEjecutadoPor(actor);
        auditoria.setDetalle(detalle);
        auditoria.setDatosAnteriores(datosAnteriores);
        auditoria.setDatosNuevos(capturarDatos(examen));
        repositorio.save(auditoria);
    }
}
