// =====================================================
// ELEMENTOS DEL PORTAL
// =====================================================

const modal =
    document.getElementById("modalDpi");

const btnAgendar =
    document.getElementById("btnAgendar");

const cerrarModal =
    document.getElementById("cerrarModal");

const btnCancelar =
    document.getElementById("btnCancelar");

const btnVerificar =
    document.getElementById("btnVerificar");

const inputDpi =
    document.getElementById("dpi");

const contador =
    document.getElementById("contador");

const mensaje =
    document.getElementById("mensaje");


// =====================================================
// ABRIR MODAL
// =====================================================

btnAgendar.addEventListener(
    "click",
    () => {

        modal.style.display =
            "block";

        inputDpi.focus();

        actualizarContador();
    }
);


// =====================================================
// CERRAR MODAL
// =====================================================

cerrarModal.addEventListener(
    "click",
    cerrar
);


btnCancelar.addEventListener(
    "click",
    cerrar
);


function cerrar() {

    modal.style.display =
        "none";

    inputDpi.value =
        "";

    contador.textContent =
        "0/13 dígitos";

    contador.style.color =
        "#666";

    mensaje.textContent =
        "";

    mensaje.style.color =
        "";
}


// =====================================================
// ACTUALIZAR CONTADOR
// =====================================================

function actualizarContador() {

    // =================================================
    // PERMITIR SOLO NÚMEROS
    // =================================================

    inputDpi.value =
        inputDpi.value.replace(
            /\D/g,
            ""
        );


    // =================================================
    // MÁXIMO 13 DÍGITOS
    // =================================================

    if (
        inputDpi.value.length > 13
    ) {

        inputDpi.value =
            inputDpi.value.substring(
                0,
                13
            );
    }


    const cantidad =
        inputDpi.value.length;


    contador.textContent =
        cantidad
        +
        "/13 dígitos";


    // =================================================
    // CAMBIAR COLOR CUANDO ESTÉ COMPLETO
    // =================================================

    if (cantidad === 13) {

        contador.style.color =
            "green";

    } else {

        contador.style.color =
            "#666";
    }
}


// =====================================================
// DETECTAR ESCRITURA, PEGADO Y CAMBIOS EN EL INPUT
// =====================================================

inputDpi.addEventListener(
    "input",
    () => {

        actualizarContador();

        // Limpiar mensajes anteriores
        mensaje.textContent =
            "";
    }
);


// =====================================================
// DETECTAR PEGADO EXPLÍCITAMENTE
//
// Aunque el evento "input" normalmente detecta el pegado,
// agregamos este evento como respaldo.
// =====================================================

inputDpi.addEventListener(
    "paste",
    () => {

        setTimeout(
            () => {

                actualizarContador();

            },
            0
        );
    }
);


// =====================================================
// VERIFICAR CON ENTER
// =====================================================

inputDpi.addEventListener(
    "keydown",
    (event) => {

        if (event.key === "Enter") {

            event.preventDefault();

            btnVerificar.click();
        }
    }
);


// =====================================================
// VERIFICAR DPI
// =====================================================

btnVerificar.addEventListener(
    "click",
    async () => {

        const dpi =
            inputDpi.value.trim();


        // =================================================
        // VALIDAR DPI
        // =================================================

        if (!/^\d{13}$/.test(dpi)) {

            mensaje.style.color =
                "red";

            mensaje.textContent =
                "El DPI debe contener exactamente 13 dígitos.";

            return;
        }


        // =================================================
        // DESHABILITAR BOTÓN
        // =================================================

        btnVerificar.disabled =
            true;

        btnVerificar.textContent =
            "Verificando...";

        mensaje.textContent =
            "";


        try {

            // =================================================
            // CONSULTAR API
            //
            // CONSERVAMOS EL POST ORIGINAL
            // =================================================

            const respuesta =
                await fetch(
                    "/api/verificar-dpi",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify(
                            {
                                dpi: dpi
                            }
                        )
                    }
                );


            // =================================================
            // VALIDAR RESPUESTA HTTP
            // =================================================

            if (!respuesta.ok) {

                throw new Error(
                    "Error al verificar el DPI."
                );
            }


            const datos =
                await respuesta.json();


            // =================================================
            // PACIENTE REGISTRADO
            // =================================================

            if (
                datos.estado ===
                "PACIENTE"
            ) {

                mensaje.style.color =
                    "green";

                mensaje.textContent =
                    "Paciente registrado. Redirigiendo...";


                setTimeout(
                    () => {

                        window.location.href =
                            "/login";

                    },
                    1200
                );
            }


                // =================================================
                // DPI NO REGISTRADO
            // =================================================

            else if (
                datos.estado ===
                "NO_REGISTRADO"
            ) {

                mensaje.style.color =
                    "#d97706";

                mensaje.textContent =
                    "No se encontró un registro asociado a este DPI. "
                    +
                    "Será redirigido al formulario de registro.";


                setTimeout(
                    () => {

                        window.location.href =
                            "/registro";

                    },
                    1500
                );
            }


                // =================================================
                // USUARIO INTERNO
            // =================================================

            else if (
                datos.estado ===
                "USUARIO_INTERNO"
            ) {

                mensaje.style.color =
                    "red";

                mensaje.textContent =
                    datos.mensaje;
            }


                // =================================================
                // CUALQUIER OTRO ESTADO
            // =================================================

            else {

                mensaje.style.color =
                    "red";

                mensaje.textContent =
                    datos.mensaje
                    ||
                    "No fue posible verificar el DPI.";
            }

        }

        catch (error) {

            console.error(
                "Error:",
                error
            );


            mensaje.style.color =
                "red";

            mensaje.textContent =
                "No se pudo conectar con el servidor. "
                +
                "Intente de nuevo más tarde.";

        }

        finally {

            btnVerificar.disabled =
                false;

            btnVerificar.textContent =
                "Verificar DPI";
        }
    }
);


// =====================================================
// CERRAR MODAL AL HACER CLIC FUERA
// =====================================================

window.addEventListener(
    "click",
    (event) => {

        if (event.target === modal) {

            cerrar();
        }
    }
);