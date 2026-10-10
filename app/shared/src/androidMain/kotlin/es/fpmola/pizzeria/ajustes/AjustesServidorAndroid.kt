package es.fpmola.pizzeria.ajustes

import android.content.Context

/**
 * Implementación de [AjustesServidor] con SharedPreferences.
 */
class AjustesServidorAndroid(contexto: Context) : AjustesServidor {

    private val preferencias = contexto.applicationContext
        .getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    override fun obtenerUrlBase(): String? =
        preferencias.getString(CLAVE_URL_BASE, null)?.takeIf { it.isNotBlank() }

    override fun guardarUrlBase(url: String) {
        preferencias.edit().putString(CLAVE_URL_BASE, url).apply()
    }

    private companion object {
        const val NOMBRE_PREFERENCIAS = "ajustes_servidor"
        const val CLAVE_URL_BASE = "url_base"
    }
}
