package es.fpmola.pizzeria.red

/**
 * Errores de comunicación con el backend, con un mensaje en español listo
 * para mostrar al usuario en [mensajeUsuario].
 */
sealed class ErrorRed(
    val mensajeUsuario: String,
    causa: Throwable? = null,
) : Exception(mensajeUsuario, causa) {

    /** Todavía no se ha guardado ninguna URL de servidor. */
    class SinServidorConfigurado : ErrorRed(
        "No hay ningún servidor configurado. Indica su dirección en la configuración.",
    )

    /** No se pudo completar la petición (sin red, DNS, TLS, tiempo de espera…). */
    class SinConexion(causa: Throwable? = null) : ErrorRed(
        "No se ha podido conectar con el servidor. Comprueba la dirección y tu conexión a Internet.",
        causa,
    )

    /** El servidor respondió con un código HTTP de error. */
    class Servidor(val codigo: Int) : ErrorRed(
        "El servidor ha respondido con un error (código $codigo).",
    )

    /** La respuesta llegó, pero no tiene el formato esperado. */
    class RespuestaInvalida(causa: Throwable? = null) : ErrorRed(
        "El servidor ha enviado una respuesta que la app no entiende.",
        causa,
    )
}
