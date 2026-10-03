package com.example.sistemamedico.service;

import com.example.sistemamedico.model.AuditoriaUsuario;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.AuditoriaUsuario_Repositorio;

import org.springframework.stereotype.Service;

@Service
public class AuditoriaUsuarioService {

    private final AuditoriaUsuario_Repositorio auditoriaRepository;

    public AuditoriaUsuarioService(
            AuditoriaUsuario_Repositorio auditoriaRepository
    ) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(
            String accion,
            Usuario usuario,
            String ejecutadoPor
    ) {

        AuditoriaUsuario auditoria =
                new AuditoriaUsuario();

        auditoria.setAccion(accion);
        auditoria.setUsuarioAfectado(usuario);
        auditoria.setNombreUsuarioAfectado(
                usuario.getNombreUsuario()
        );
        auditoria.setEjecutadoPor(ejecutadoPor);

        auditoria.setDetalle(
                "Se " +
                        ("CREAR".equals(accion)
                                ? "creó"
                                : "actualizó")
                        + " el usuario "
                        + usuario.getNombreUsuario()
        );

        auditoriaRepository.save(auditoria);
    }

    public void registrarEliminacion(
            String nombreUsuario,
            String ejecutadoPor
    ) {

        AuditoriaUsuario auditoria =
                new AuditoriaUsuario();

        auditoria.setAccion("ELIMINAR");
        auditoria.setUsuarioAfectado(null);
        auditoria.setNombreUsuarioAfectado(nombreUsuario);
        auditoria.setEjecutadoPor(ejecutadoPor);
        auditoria.setDetalle(
                "Se eliminó el usuario " + nombreUsuario
        );

        auditoriaRepository.save(auditoria);
    }
}
