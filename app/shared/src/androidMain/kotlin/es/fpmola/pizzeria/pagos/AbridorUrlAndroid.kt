package es.fpmola.pizzeria.pagos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre la URL en el navegador del móvil con un Intent ACTION_VIEW.
 *
 * Se usa el contexto de la aplicación, que no es una actividad, así que el
 * Intent necesita FLAG_ACTIVITY_NEW_TASK. Solo abre URLs https de Stripe
 * (ver [esUrlSegura]); se vuelve a comprobar el host con el analizador de
 * Android, que es el que interpretará la URL.
 */
class AbridorUrlAndroid(private val contexto: Context) : AbridorUrl {

    override fun abrir(url: String): ResultadoAbrirUrl {
        if (!esUrlSegura(url)) return ResultadoAbrirUrl.Fallida(MENSAJE_URL_NO_SEGURA)

        val uri = Uri.parse(url)
        val esHttps = uri.scheme.equals("https", ignoreCase = true)
        if (!esHttps || !esHostDeStripe(uri.host)) return ResultadoAbrirUrl.Fallida(MENSAJE_URL_NO_SEGURA)

        val intento = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            contexto.startActivity(intento)
            ResultadoAbrirUrl.Abierta
        } catch (e: ActivityNotFoundException) {
            ResultadoAbrirUrl.Fallida("No se ha encontrado ningún navegador para abrir la página de pago.")
        }
    }
}
