package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.Pago_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.HorarioMedico;
import com.example.sistemamedico.model.Pago;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class PagoService {

    private final Pago_Repositorio pagoRepository;
    private final Cita_Repositorio citaRepository;
    private final HorarioMedico_Repositorio horarioMedicoRepository;
    private final CorreoService correoService;
    private final PasarelaPagoService pasarelaPagoService;


    public PagoService(
            Pago_Repositorio pagoRepository,
            Cita_Repositorio citaRepository,
            HorarioMedico_Repositorio horarioMedicoRepository,
            CorreoService correoService,
            PasarelaPagoService pasarelaPagoService
    ) {

        this.pagoRepository = pagoRepository;
        this.citaRepository = citaRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.correoService = correoService;
        this.pasarelaPagoService = pasarelaPagoService;
    }


    // =====================================================
    // BUSCAR CITA
    // =====================================================

    public Cita buscarCita(
            Long citaId
    ) {

        return citaRepository
                .findById(citaId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Cita no encontrada."
                        )
                );
    }


    // =====================================================
    // BUSCAR PAGO
    // =====================================================

    public Pago buscarPago(
            Long pagoId
    ) {

        return pagoRepository
                .findById(pagoId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Pago no encontrado."
                        )
                );
    }


    // =====================================================
    // OBTENER MONTO
    // =====================================================

    public BigDecimal obtenerMonto(
            Cita cita
    ) {

        if (
                cita.getEspecialidad() == null
                        ||
                        cita.getEspecialidad().getPrecio() == null
        ) {

            throw new IllegalStateException(
                    "La especialidad no tiene un precio configurado."
            );
        }


        return cita.getEspecialidad()
                .getPrecio();
    }


    // =====================================================
    // VALIDAR NÚMERO DE TARJETA
    // =====================================================

    public String validarNumeroTarjeta(
            String numeroTarjeta
    ) {

        String numero =
                numeroTarjeta == null
                        ? ""
                        : numeroTarjeta.replaceAll(
                        "\\s+",
                        ""
                );


        if (!numero.matches("\\d{13,19}")) {

            return "El número de tarjeta debe contener entre 13 y 19 dígitos.";
        }


        if (!validarLuhn(numero)) {

            return "El número de tarjeta no es válido.";
        }


        return null;
    }


    // =====================================================
    // ALGORITMO DE LUHN
    // =====================================================

    private boolean validarLuhn(
            String numero
    ) {

        int suma = 0;

        boolean duplicar = false;


        for (
                int i = numero.length() - 1;
                i >= 0;
                i--
        ) {

            int digito =
                    Character.getNumericValue(
                            numero.charAt(i)
                    );


            if (duplicar) {

                digito *= 2;


                if (digito > 9) {

                    digito -= 9;
                }
            }


            suma += digito;

            duplicar = !duplicar;
        }


        return suma % 10 == 0;
    }


    // =====================================================
    // VALIDAR TITULAR
    // =====================================================

    public String validarTitular(
            String titular
    ) {

        String valor =
                titular == null
                        ? ""
                        : titular.trim();


        if (
                valor.length() < 5
                        ||
                        valor.length() > 100
        ) {

            return "El nombre del titular debe contener entre 5 y 100 caracteres.";
        }


        return null;
    }


    // =====================================================
    // VALIDAR VENCIMIENTO
    // =====================================================

    public String validarVencimiento(
            String vencimiento
    ) {

        if (
                vencimiento == null
                        ||
                        !vencimiento.matches("\\d{2}/\\d{2}")
        ) {

            return "Formato inválido. Use MM/AA.";
        }


        try {

            String[] partes =
                    vencimiento.split("/");


            int mes =
                    Integer.parseInt(
                            partes[0]
                    );


            int anio =
                    2000
                            +
                            Integer.parseInt(
                                    partes[1]
                            );


            if (
                    mes < 1
                            ||
                            mes > 12
            ) {

                return "Formato inválido. Use MM/AA.";
            }


            YearMonth fechaVencimiento =
                    YearMonth.of(
                            anio,
                            mes
                    );


            YearMonth actual =
                    YearMonth.now();


            if (
                    fechaVencimiento
                            .isBefore(
                                    actual
                            )
            ) {

                return "La tarjeta está vencida.";
            }


        } catch (Exception e) {

            return "Formato inválido. Use MM/AA.";
        }


        return null;
    }


    // =====================================================
    // VALIDAR CVV
    // =====================================================

    public String validarCvv(
            String cvv
    ) {

        if (
                cvv == null
                        ||
                        !cvv.matches("\\d{3,4}")
        ) {

            return "El CVV debe contener 3 o 4 dígitos.";
        }


        return null;
    }


    // =====================================================
    // VERIFICAR RESERVA VIGENTE
    // =====================================================

    public boolean reservaVigente(
            Cita cita
    ) {

        HorarioMedico horario =
                cita.getHorario();


        if (
                horario == null
                        ||
                        horario.getReservadoHasta() == null
        ) {

            return false;
        }


        return LocalDateTime.now()
                .isBefore(
                        horario.getReservadoHasta()
                );
    }


    // =====================================================
    // GENERAR IDEMPOTENCY KEY
    // =====================================================

    public String generarIdempotencyKey() {

        return UUID.randomUUID()
                .toString();
    }


    // =====================================================
    // PROCESAR PAGO
    // =====================================================

    @Transactional
    public Pago procesarPago(
            Long citaId,
            String numeroTarjeta,
            String titular,
            String vencimiento,
            String cvv,
            String idempotencyKey
    ) {

        // =================================================
        // IDEMPOTENCIA
        // =================================================
        // SE REVISA PRIMERO PARA EVITAR COBROS DUPLICADOS
        // =================================================

        if (
                idempotencyKey != null
                        &&
                        pagoRepository
                                .existsByIdempotencyKey(
                                        idempotencyKey
                                )
        ) {

            return pagoRepository
                    .findByIdempotencyKey(
                            idempotencyKey
                    )
                    .orElseThrow(
                            () -> new IllegalStateException(
                                    "No se pudo recuperar el pago existente."
                            )
                    );
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                buscarCita(
                        citaId
                );


        // =================================================
        // EVITAR PAGAR DOS VECES UNA CITA
        // =================================================

        if (
                "PAGADA".equals(
                        cita.getEstado()
                )
        ) {

            throw new IllegalStateException(
                    "La cita ya se encuentra pagada."
            );
        }


        // =================================================
        // VALIDAR RESERVA
        // =================================================

        if (!reservaVigente(cita)) {

            throw new IllegalStateException(
                    "La reserva de la cita ha expirado."
            );
        }


        // =================================================
        // LIMPIAR NÚMERO DE TARJETA
        // =================================================

        String numeroLimpio =
                numeroTarjeta
                        .replaceAll(
                                "\\s+",
                                ""
                        );


        // =================================================
        // PASARELA DE PAGO SIMULADA
        // =================================================

        PasarelaPagoService.EstadoPasarela resultado =
                pasarelaPagoService.procesar(
                        numeroLimpio
                );


        // =================================================
        // RECHAZO BANCARIO
        // =================================================

        if (
                resultado
                        ==
                        PasarelaPagoService.EstadoPasarela.RECHAZADO
        ) {

            throw new IllegalStateException(
                    "La transacción con tarjeta fue rechazada por el banco. " +
                            "Por favor, verifique los datos de su tarjeta o intente con una tarjeta diferente."
            );
        }


        // =================================================
        // ERROR DE PROCESAMIENTO
        // =================================================

        if (
                resultado
                        ==
                        PasarelaPagoService.EstadoPasarela.ERROR_PROCESAMIENTO
        ) {

            throw new IllegalStateException(
                    "El pago no pudo ser procesado. " +
                            "Por favor, intente nuevamente o utilice otra tarjeta."
            );
        }


        // =================================================
        // ERROR DE COMUNICACIÓN
        // =================================================

        if (
                resultado
                        ==
                        PasarelaPagoService.EstadoPasarela.ERROR_COMUNICACION
        ) {

            throw new IllegalStateException(
                    "Error de comunicación con la pasarela de pago. " +
                            "Intente nuevamente en unos minutos."
            );
        }


        // =================================================
        // CREAR PAGO
        // =================================================

        Pago pago =
                new Pago();


        pago.setCita(
                cita
        );


        pago.setMonto(
                obtenerMonto(
                        cita
                )
        );


        // =================================================
        // GENERAR NÚMERO DE TRANSACCIÓN
        // =================================================

        pago.setNumeroTransaccion(
                "TXN-"
                        +
                        UUID.randomUUID()
                                .toString()
                                .substring(
                                        0,
                                        8
                                )
                                .toUpperCase()
        );


        pago.setEstado(
                "PAGADO"
        );


        // =================================================
        // GUARDAR ÚNICAMENTE ÚLTIMOS 4 DÍGITOS
        // =================================================

        pago.setUltimosCuatro(
                numeroLimpio.substring(
                        numeroLimpio.length() - 4
                )
        );


        // =================================================
        // DETECTAR MARCA DE TARJETA
        // =================================================

        pago.setMarcaTarjeta(
                detectarMarcaTarjeta(
                        numeroLimpio
                )
        );


        // =================================================
        // GUARDAR IDEMPOTENCY KEY
        // =================================================

        pago.setIdempotencyKey(
                idempotencyKey
        );


        // =================================================
        // FECHA DE PAGO
        // =================================================

        pago.setFechaPago(
                LocalDateTime.now()
        );


        // =================================================
        // ACTUALIZAR CITA A PAGADA
        // =================================================

        cita.setEstado(
                "CONFIRMADA"
        );

        citaRepository.save(
                cita
        );


        // =================================================
        // ACTUALIZAR HORARIO
        // RESERVADO -> OCUPADO
        // =================================================

        HorarioMedico horario =
                cita.getHorario();


        horario.setEstado(
                "OCUPADO"
        );


        horario.setReservadoHasta(
                null
        );


        horarioMedicoRepository.save(
                horario
        );


        // =================================================
        // GUARDAR PAGO
        // =================================================

        Pago pagoGuardado =
                pagoRepository.save(
                        pago
                );


        // =================================================
        // ENVIAR COMPROBANTE DE PAGO
        // =================================================

        correoService.enviarComprobantePago(
                pagoGuardado
        );


        return pagoGuardado;
    }


    // =====================================================
    // DETECTAR MARCA DE TARJETA
    // =====================================================

    private String detectarMarcaTarjeta(
            String numero
    ) {

        if (numero.startsWith("4")) {

            return "VISA";
        }


        if (
                numero.startsWith("51")
                        ||
                        numero.startsWith("52")
                        ||
                        numero.startsWith("53")
                        ||
                        numero.startsWith("54")
                        ||
                        numero.startsWith("55")
        ) {

            return "MASTERCARD";
        }


        return "OTRA";
    }
}


