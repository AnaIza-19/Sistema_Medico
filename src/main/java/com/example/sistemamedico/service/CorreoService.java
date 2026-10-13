package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Service
public class CorreoService {

    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String correoRemitente;


    public CorreoService(
            JavaMailSender mailSender
    ) {

        this.mailSender = mailSender;
    }


    // =====================================================
    // CU-02
    // CORREO DE BIENVENIDA
    // =====================================================

    public void enviarCorreoBienvenida(
            String destinatario,
            String nombrePaciente
    ) {

        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                destinatario
        );


        mensaje.setSubject(
                "Bienvenido al Sistema de Citas - Hospital Sistema Médico"
        );


        mensaje.setText(
                "Estimado(a) "
                        + nombrePaciente
                        + ", su registro ha sido completado exitosamente. "
                        + "Ya puede agendar sus citas médicas a través de nuestro portal."
        );


        mailSender.send(
                mensaje
        );
    }


    // =====================================================
    // CU-04
    // COMPROBANTE DE PAGO
    // =====================================================

    public void enviarComprobantePago(
            Pago pago
    ) {

        String correoPaciente =
                pago.getCita()
                        .getPaciente()
                        .getCorreo();


        String nombrePaciente =
                pago.getCita()
                        .getPaciente()
                        .getNombre()
                        + " "
                        + pago.getCita()
                        .getPaciente()
                        .getApellido();


        String medico =
                pago.getCita()
                        .getMedico()
                        .getNombre()
                        + " "
                        + pago.getCita()
                        .getMedico()
                        .getApellido();


        String especialidad =
                pago.getCita()
                        .getEspecialidad()
                        .getNombre();


        String sucursal =
                pago.getCita()
                        .getSucursal()
                        .getNombre();


        String fecha =
                pago.getCita()
                        .getHorario()
                        .getFecha()
                        .toString();


        String hora =
                pago.getCita()
                        .getHorario()
                        .getHoraInicio()
                        + " - "
                        + pago.getCita()
                        .getHorario()
                        .getHoraFin();


        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                correoPaciente
        );


        mensaje.setSubject(
                "Comprobante de pago - Sistema Médico Hospitalario"
        );


        mensaje.setText(
                "Estimado(a) " + nombrePaciente + ",\n\n"
                        + "Su pago ha sido realizado exitosamente.\n\n"
                        + "COMPROBANTE DE PAGO\n"
                        + "----------------------------------\n"
                        + "Número de transacción: "
                        + pago.getNumeroTransaccion() + "\n"
                        + "Médico: "
                        + medico + "\n"
                        + "Especialidad: "
                        + especialidad + "\n"
                        + "Sucursal: "
                        + sucursal + "\n"
                        + "Fecha: "
                        + fecha + "\n"
                        + "Hora: "
                        + hora + "\n"
                        + "Monto pagado: Q"
                        + pago.getMonto() + "\n"
                        + "Tarjeta terminada en: **** "
                        + pago.getUltimosCuatro() + "\n"
                        + "----------------------------------\n\n"
                        + "Su cita ha sido confirmada.\n\n"
                        + "Gracias por utilizar el Sistema Médico Hospitalario."
        );


        mailSender.send(
                mensaje
        );
    }


    // =====================================================
    // CU-12
    // CORREO DE CONFIRMACIÓN DE CITA DE SEGUIMIENTO
    // =====================================================

    public void enviarConfirmacionSeguimiento(
            Cita cita
    ) {

        // =================================================
        // VALIDAR DATOS NECESARIOS
        // =================================================

        if (
                cita == null
                        ||
                        cita.getPaciente() == null
                        ||
                        cita.getHorario() == null
                        ||
                        cita.getMedico() == null
                        ||
                        cita.getSucursal() == null
        ) {

            throw new IllegalArgumentException(
                    "No se pudo generar la notificación de la cita de seguimiento."
            );
        }


        String correoPaciente =
                cita.getPaciente()
                        .getCorreo();


        if (
                correoPaciente == null
                        ||
                        correoPaciente.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El paciente no tiene un correo electrónico registrado."
            );
        }


        // =================================================
        // DATOS DEL PACIENTE
        // =================================================

        String nombrePaciente =
                cita.getPaciente()
                        .getNombre()
                        + " "
                        + cita.getPaciente()
                        .getApellido();


        // =================================================
        // DATOS DEL MÉDICO
        // =================================================

        String medico =
                cita.getMedico()
                        .getNombre()
                        + " "
                        + cita.getMedico()
                        .getApellido();


        // =================================================
        // SUCURSAL
        // =================================================

        String sucursal =
                cita.getSucursal()
                        .getNombre();


        // =================================================
        // FECHA
        // =================================================

        DateTimeFormatter formatoFecha =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );


        String fecha =
                cita.getHorario()
                        .getFecha()
                        .format(
                                formatoFecha
                        );


        // =================================================
        // HORA
        // =================================================

        DateTimeFormatter formatoHora =
                DateTimeFormatter.ofPattern(
                        "HH:mm"
                );


        String hora =
                cita.getHorario()
                        .getHoraInicio()
                        .format(
                                formatoHora
                        )
                        + " - "
                        +
                        cita.getHorario()
                                .getHoraFin()
                                .format(
                                        formatoHora
                                );


        // =================================================
        // TIPO DE SEGUIMIENTO
        // =================================================

        String tipoSeguimiento;


        if (
                "MONITOREO_TRATAMIENTO"
                        .equalsIgnoreCase(
                                cita.getTipoSeguimiento()
                        )
        ) {

            tipoSeguimiento =
                    "Monitoreo de Tratamiento";

        } else if (
                "REVISION_RESULTADOS_LABORATORIO"
                        .equalsIgnoreCase(
                                cita.getTipoSeguimiento()
                        )
        ) {

            tipoSeguimiento =
                    "Revisión de Resultados de Laboratorio";

        } else {

            tipoSeguimiento =
                    "Seguimiento médico";
        }


        // =================================================
        // OBSERVACIONES
        // =================================================

        String observaciones =
                cita.getMotivoConsulta() == null
                        ? ""
                        : cita.getMotivoConsulta();


        // =================================================
        // CREAR CORREO
        // =================================================

        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                correoPaciente
        );


        mensaje.setSubject(
                "Cita de Seguimiento Agendada - Hospital Sistema Médico"
        );


        mensaje.setText(

                "Estimado(a) "
                        + nombrePaciente
                        + ",\n\n"

                        + "Su cita de seguimiento ha sido agendada exitosamente.\n\n"

                        + "DATOS DE LA CITA DE SEGUIMIENTO\n"
                        + "----------------------------------\n"

                        + "Fecha: "
                        + fecha
                        + "\n"

                        + "Hora: "
                        + hora
                        + "\n"

                        + "Tipo de seguimiento: "
                        + tipoSeguimiento
                        + "\n"

                        + "Médico: "
                        + medico
                        + "\n"

                        + "Sucursal: "
                        + sucursal
                        + "\n"

                        + "Observaciones: "
                        + observaciones
                        + "\n"

                        + "----------------------------------\n\n"

                        + "Por favor, preséntese con anticipación "
                        + "el día de su cita.\n\n"

                        + "Este es un correo automático del "
                        + "Sistema Informático Hospitalario. "
                        + "No responda a este mensaje.\n\n"

                        + "Gracias por utilizar el "
                        + "Sistema Médico Hospitalario."
        );


        mailSender.send(
                mensaje
        );
    }
    // =====================================================
// CU-12
// RECORDATORIO DE CITA DE SEGUIMIENTO
// =====================================================

    public void enviarRecordatorioSeguimiento(
            Cita cita
    ) {

        if (
                cita == null
                        ||
                        cita.getPaciente() == null
                        ||
                        cita.getHorario() == null
                        ||
                        cita.getMedico() == null
                        ||
                        cita.getSucursal() == null
        ) {

            throw new IllegalArgumentException(
                    "No se pudo generar el recordatorio de la cita de seguimiento."
            );
        }


        String correoPaciente =
                cita.getPaciente()
                        .getCorreo();


        if (
                correoPaciente == null
                        ||
                        correoPaciente.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El paciente no tiene un correo electrónico registrado."
            );
        }


        String nombrePaciente =
                cita.getPaciente()
                        .getNombre()
                        + " "
                        + cita.getPaciente()
                        .getApellido();


        String medico =
                cita.getMedico()
                        .getNombre()
                        + " "
                        + cita.getMedico()
                        .getApellido();


        String sucursal =
                cita.getSucursal()
                        .getNombre();


        DateTimeFormatter formatoFecha =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );


        String fecha =
                cita.getHorario()
                        .getFecha()
                        .format(
                                formatoFecha
                        );


        DateTimeFormatter formatoHora =
                DateTimeFormatter.ofPattern(
                        "HH:mm"
                );


        String hora =
                cita.getHorario()
                        .getHoraInicio()
                        .format(
                                formatoHora
                        )
                        + " - "
                        +
                        cita.getHorario()
                                .getHoraFin()
                                .format(
                                        formatoHora
                                );


        String tipoSeguimiento;


        if (
                "MONITOREO_TRATAMIENTO"
                        .equalsIgnoreCase(
                                cita.getTipoSeguimiento()
                        )
        ) {

            tipoSeguimiento =
                    "Monitoreo de Tratamiento";

        } else if (
                "REVISION_RESULTADOS_LABORATORIO"
                        .equalsIgnoreCase(
                                cita.getTipoSeguimiento()
                        )
        ) {

            tipoSeguimiento =
                    "Revisión de Resultados de Laboratorio";

        } else {

            tipoSeguimiento =
                    "Seguimiento médico";
        }


        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                correoPaciente
        );


        mensaje.setSubject(
                "Recordatorio: Su Cita de Seguimiento Mañana"
        );


        mensaje.setText(
                "Estimado(a) "
                        + nombrePaciente
                        + ",\n\n"

                        + "Le recordamos que tiene una cita de seguimiento programada.\n\n"

                        + "DATOS DE LA CITA\n"
                        + "----------------------------------\n"

                        + "Fecha: "
                        + fecha
                        + "\n"

                        + "Hora: "
                        + hora
                        + "\n"

                        + "Tipo de seguimiento: "
                        + tipoSeguimiento
                        + "\n"

                        + "Médico: "
                        + medico
                        + "\n"

                        + "Sucursal: "
                        + sucursal
                        + "\n"

                        + "----------------------------------\n\n"

                        + "Por favor, preséntese con anticipación "
                        + "el día de su cita.\n\n"

                        + "Este es un correo automático del "
                        + "Sistema Informático Hospitalario. "
                        + "No responda a este mensaje.\n\n"

                        + "Gracias por utilizar el "
                        + "Sistema Médico Hospitalario."
        );


        mailSender.send(
                mensaje
        );
    }
}