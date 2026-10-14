
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgendaMedicaSeguridadService {

    private final PermisoRolService permisoRolService;

    public AgendaMedicaSeguridadService(
            PermisoRolService permisoRolService
    ) {
        this.permisoRolService = permisoRolService;
    }

    // =====================================================
    // CU-16: IDENTIFICAR MÉDICO AUTENTICADO
    // =====================================================

    public Usuario obtenerMedico(HttpSession session) {

        Object valor =
                session.getAttribute("usuarioInterno");

        if (!(valor instanceof Usuario usuario)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debe iniciar sesión como médico."
            );
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || usuario.getRol() == null
                || !"MEDICO".equalsIgnoreCase(
                usuario.getRol().getNombre()
        )
                || usuario.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso permitido únicamente a médicos activos."
            );
        }

        return usuario;
    }

    // =====================================================
    // CU-16: COMPROBAR PERMISO ESPECÍFICO
    // =====================================================

    public Usuario exigirPermiso(
            HttpSession session,
            String accion
    ) {

        Usuario medico = obtenerMedico(session);

        if (accion == null || !(
                accion.equals("read")
                        || accion.equals("create")
                        || accion.equals("update")
                        || accion.equals("delete")
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Operación no autorizada."
            );
        }

        String codigoPermiso =
                "medical-agenda/" + accion;

        if (!permisoRolService.tienePermiso(
                medico,
                codigoPermiso
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene el permiso: " + codigoPermiso
            );
        }

        return medico;
    }

    // =====================================================
    // CU-16: OBTENER ID DEL MÉDICO AUTORIZADO
    // =====================================================

    public Long obtenerMedicoId(
            HttpSession session,
            String accion
    ) {

        return exigirPermiso(
                session,
                accion
        ).getId();
    }
}
