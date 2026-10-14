package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaEstadoCita_Repositorio;
import com.example.sistemamedico.model.AuditoriaEstadoCita;
import com.example.sistemamedico.model.EstadoCita;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaEstadoCitaService {

    private final AuditoriaEstadoCita_Repositorio auditoriaRepository;

    public AuditoriaEstadoCitaService(AuditoriaEstadoCita_Repositorio auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public String capturarDatos(EstadoCita estado) {
        if (estado == null) return null;
        return "ID: " + estado.getId()
                + " | Código: " + estado.getCodigo()
                + " | Nombre: " + estado.getNombre()
                + " | Descripción: " + estado.getDescripcion()
                + " | Activo: " + estado.getActivo()
                + " | Es sistema: " + estado.getEsSistema();
    }

    public void registrar(String accion, EstadoCita estado,
                          String ejecutadoPor, String datosAnteriores, String detalle) {
        AuditoriaEstadoCita auditoria = new AuditoriaEstadoCita();
        auditoria.setEstadoCitaId(estado.getId());
        auditoria.setCodigoEstado(estado.getCodigo());
        auditoria.setAccion(accion);
        auditoria.setEjecutadoPor(ejecutadoPor);
        auditoria.setDetalle(detalle);
        auditoria.setDatosAnteriores(datosAnteriores);
        auditoria.setDatosNuevos(capturarDatos(estado));
        auditoriaRepository.save(auditoria);
    }
}
