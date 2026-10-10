package es.fpmola.pizzeria.pagos

/** Resultado de intentar abrir una URL en el navegador del móvil. */
sealed interface ResultadoAbrirUrl {

    /** Se pidió al sistema que abriera la URL en el navegador. */
    data object Abierta : ResultadoAbrirUrl

    /** No se abrió nada; [mensaje] explica el motivo en español. */
    data class Fallida(val mensaje: String) : ResultadoAbrirUrl
}

/**
 * Abre una URL en el navegador del móvil. Cada plataforma aporta su
 * implementación (en Android, un Intent ACTION_VIEW).
 *
 * Las implementaciones solo pueden abrir URLs que pasen [esUrlSegura]: son
 * https y su host es de Stripe. Si no, devuelven [ResultadoAbrirUrl.Fallida]
 * y no abren nada.
 */
interface AbridorUrl {
    fun abrir(url: String): ResultadoAbrirUrl
}

const val MENSAJE_URL_NO_SEGURA =
    "La dirección de pago que ha enviado el servidor no es de Stripe, así que no se ha abierto."

private const val ESQUEMA_HTTPS = "https://"
private const val DOMINIO_STRIPE = "stripe.com"
private const val PUERTO_LONGITUD_MAXIMA = 5

/**
 * true si [url] es https y su host es `stripe.com` o termina en `.stripe.com`.
 *
 * Es un análisis de texto propio y estricto, sin usar el analizador de URL de
 * ninguna librería: la autoridad (lo que hay entre `https://` y la primera
 * `/`, `?` o `#`) solo puede tener letras, números, puntos y guiones, y un
 * puerto opcional. Así `https://stripe.com@evil.com`, `https://evil.com\@stripe.com`
 * o un host con caracteres raros se rechazan, y no puede haber diferencias
 * con el analizador del sistema que abre la URL.
 */
fun esUrlSegura(url: String): Boolean {
    if (!url.startsWith(ESQUEMA_HTTPS, ignoreCase = true)) return false
    val autoridad = url.substring(ESQUEMA_HTTPS.length).takeWhile { it != '/' && it != '?' && it != '#' }

    val separador = autoridad.indexOf(':')
    val host = if (separador >= 0) autoridad.substring(0, separador) else autoridad
    if (separador >= 0) {
        val puerto = autoridad.substring(separador + 1)
        val puertoValido = puerto.isNotEmpty() &&
            puerto.length <= PUERTO_LONGITUD_MAXIMA &&
            puerto.all { it in '0'..'9' }
        if (!puertoValido) return false
    }
    return esHostDeStripe(host)
}

/**
 * true si [host] es `stripe.com` o un subdominio suyo (`checkout.stripe.com`).
 * No admite caracteres fuera de letras, números, puntos y guiones, ni un
 * punto inicial ni dos puntos seguidos.
 */
fun esHostDeStripe(host: String?): Boolean {
    if (host == null) return false
    val minusculas = host.lowercase()
    val caracteresValidos = minusculas.all { it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '-' }
    if (!caracteresValidos || minusculas.startsWith(".") || minusculas.contains("..")) return false
    return minusculas == DOMINIO_STRIPE || minusculas.endsWith(".$DOMINIO_STRIPE")
}
