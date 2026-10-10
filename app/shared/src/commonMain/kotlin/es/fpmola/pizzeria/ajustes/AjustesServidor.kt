package es.fpmola.pizzeria.ajustes

/**
 * Ajustes del servidor guardados en el dispositivo.
 *
 * La URL base del backend la introduce el usuario en la pantalla de
 * configuración; nunca se escribe en el código.
 */
interface AjustesServidor {

    /** URL base guardada (por ejemplo "https://dam-01.ejemplo.org"), o null si no hay. */
    fun obtenerUrlBase(): String?

    /** Guarda la URL base, ya validada y sin barra final. */
    fun guardarUrlBase(url: String)
}
