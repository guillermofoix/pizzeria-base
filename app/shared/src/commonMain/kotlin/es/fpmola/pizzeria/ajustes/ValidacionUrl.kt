package es.fpmola.pizzeria.ajustes

/** Resultado de validar la URL del servidor que escribe el usuario. */
sealed interface ValidacionUrl {
    /** URL aceptada y normalizada (https://, sin espacios ni barra final). */
    data class Valida(val url: String) : ValidacionUrl

    /** URL rechazada, con el motivo para el usuario. */
    data class NoValida(val motivo: String) : ValidacionUrl
}

private const val PREFIJO_HTTPS = "https://"

/**
 * Valida y normaliza la URL base del servidor.
 *
 * Exige https (la app no permite tráfico sin cifrar), quita espacios
 * alrededor y las barras finales.
 */
fun validarUrlServidor(texto: String): ValidacionUrl {
    val limpio = texto.trim()
    return when {
        limpio.isEmpty() ->
            ValidacionUrl.NoValida("Escribe la dirección del servidor.")
        limpio.any { it.isWhitespace() } ->
            ValidacionUrl.NoValida("La dirección no puede contener espacios.")
        limpio.startsWith("http://", ignoreCase = true) ->
            ValidacionUrl.NoValida("Solo se permiten conexiones seguras: la dirección debe empezar por https://")
        !limpio.startsWith(PREFIJO_HTTPS, ignoreCase = true) ->
            ValidacionUrl.NoValida("La dirección debe empezar por https://")
        else -> {
            val resto = limpio.substring(PREFIJO_HTTPS.length).trimEnd('/')
            when {
                resto.isEmpty() || resto.startsWith("/") ->
                    ValidacionUrl.NoValida("Falta el nombre del servidor después de https://")
                resto.contains('?') || resto.contains('#') ->
                    ValidacionUrl.NoValida("La dirección no puede llevar parámetros (? o #).")
                else -> ValidacionUrl.Valida(PREFIJO_HTTPS + resto)
            }
        }
    }
}
