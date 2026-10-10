package es.fpmola.pizzeria.catalogo

import es.fpmola.pizzeria.modelo.RespuestaApi
import es.fpmola.pizzeria.red.ClienteApi
import es.fpmola.pizzeria.red.ErrorRed

/**
 * Acceso al catálogo del backend: la carta y sus categorías.
 *
 * Todos los fallos se lanzan como [ErrorRed], con el mensaje en español en
 * [ErrorRed.mensajeUsuario].
 */
class RepositorioCatalogo(private val cliente: ClienteApi) {

    /**
     * Carta completa (GET /api/pizzas), con las pizzas agotadas incluidas.
     *
     * @throws ErrorRed si no hay servidor, no hay conexión, el servidor falla
     * o la respuesta no tiene el formato esperado.
     */
    suspend fun obtenerPizzas(): List<Pizza> =
        datosDe(cliente.obtener<RespuestaApi<List<Pizza>>>(RUTA_PIZZAS))

    /**
     * Categorías e ingredientes (GET /api/pizzas/metadata).
     *
     * @throws ErrorRed igual que [obtenerPizzas].
     */
    suspend fun obtenerMetadatos(): MetadatosCatalogo =
        datosDe(cliente.obtener<RespuestaApi<MetadatosCatalogo>>(RUTA_METADATOS))

    /** Una respuesta sin éxito o sin datos se trata como respuesta inválida. */
    private fun <T> datosDe(respuesta: RespuestaApi<T>): T {
        val datos = respuesta.data
        if (!respuesta.success || datos == null) {
            throw ErrorRed.RespuestaInvalida()
        }
        return datos
    }

    companion object {
        const val RUTA_PIZZAS = "/api/pizzas"
        const val RUTA_METADATOS = "/api/pizzas/metadata"
    }
}
