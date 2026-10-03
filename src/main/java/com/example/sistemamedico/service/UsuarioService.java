package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final Usuario_Repositorio usuarioRepository;

    public UsuarioService(Usuario_Repositorio usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String verificarDpi(String dpi) {

        // Validar que tenga exactamente 13 dígitos
        if (dpi == null || !dpi.matches("\\d{13}")) {
            return "DPI_INVALIDO";
        }

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByDpi(dpi);

        if (usuarioEncontrado.isEmpty()) {
            return "NO_REGISTRADO";
        }

        Usuario usuario = usuarioEncontrado.get();

        if (usuario.getRol() != null &&
                "PACIENTE".equalsIgnoreCase(
                        usuario.getRol().getNombre())) {

            return "PACIENTE";
        }

        return "USUARIO_INTERNO";
    }
}
