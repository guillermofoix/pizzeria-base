package es.fpmola.pizzeria.pagos

/** Por qué falló una operación de pago, con un mensaje en español para el usuario. */
sealed class ErrorPago(val mensajeUsuario: String) {

    /** La petición no llegó a completarse (sin red, DNS, TLS, tiempo de espera…). */
    data object SinConexion : ErrorPago(
        "No se ha podido conectar con el servidor. Comprueba tu conexión a Internet.",
    )

    /** El servidor dice que el pedido ya estaba pagado. */
    data object PedidoYaPagado : ErrorPago("Este pedido ya está pagado.")

    /** El servidor no tiene configurada la pasarela de pago (503). */
    data object PagoNoDisponible : ErrorPago(
        "El pago con tarjeta no está disponible ahora mismo. Pregunta en el local cómo pagar tu pedido.",
    )

    /** Cualquier otro fallo (pedido inexistente, error del servidor, respuesta ilegible…). */
    data class Otro(val mensaje: String) : ErrorPago(mensaje)
}

/** Resultado de pedir una sesión de pago (POST /api/pagos/crear-sesion). */
sealed interface ResultadoSesionPago {

    /** El servidor creó la sesión: [url] es la página de pago de Stripe. */
    data class Creada(val url: String, val idSesion: String?) : ResultadoSesionPago

    data class Fallida(val error: ErrorPago) : ResultadoSesionPago
}

/** Resultado de preguntar al servidor si un pedido está pagado (POST /api/pagos/confirmar-sesion). */
sealed interface ResultadoConfirmacionPago {

    /** El servidor comprobó con Stripe que el pago se completó y marcó el pedido como pagado. */
    data object Pagado : ResultadoConfirmacionPago

    /** Stripe todavía no ha cobrado. [estadoPago] es el valor que devuelve el servidor (por ejemplo "unpaid"). */
    data class TodaviaSinPagar(val estadoPago: String) : ResultadoConfirmacionPago

    data class Fallida(val error: ErrorPago) : ResultadoConfirmacionPago
}
