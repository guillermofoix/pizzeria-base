package es.fpmola.pizzeria.salud

import es.fpmola.pizzeria.red.ClienteApi
import es.fpmola.pizzeria.red.ErrorRed
import es.fpmola.pizzeria.red.leerCuerpo
import io.ktor.http.HttpStatusCode

/** Resultado de comprobar la salud del servidor. */
sealed interface ResultadoSalud {
    data class Correcta(val salud: SaludServidor) : ResultadoSalud
    data class Fallida(val mensaje: String) : ResultadoSalud
}

/**
 * Comprueba si el backend y su base de datos están operativos (GET /api/health).
 */
class RepositorioSalud(private val cliente: ClienteApi) {

    /**
     * La conexión es correcta solo si el HTTP es 200, status es "UP" y
     * database.connected es true. Nunca lanza [ErrorRed]: lo convierte en
     * [ResultadoSalud.Fallida] con el mensaje para el usuario.
     *
     * @param urlBase URL que se quiere probar; si es null se usa la guardada.
     */
    suspend fun comprobar(urlBase: String? = null): ResultadoSalud =
        try {
            val respuesta = cliente.peticionGet(RUTA_SALUD, urlBase)
            when (respuesta.status.value) {
                HttpStatusCode.OK.value -> {
                    val salud = respuesta.leerCuerpo<SaludServidor>()
                    when {
                        salud.operativo -> ResultadoSalud.Correcta(salud)
                        !salud.baseDatos.conectada -> ResultadoSalud.Fallida(MENSAJE_BD_CAIDA)
                        else -> ResultadoSalud.Fallida("El servidor no está operativo (estado: ${salud.estado}).")
                    }
                }
                HttpStatusCode.InternalServerError.value -> ResultadoSalud.Fallida(MENSAJE_BD_CAIDA)
                else -> ResultadoSalud.Fallida(ErrorRed.Servidor(respuesta.status.value).mensajeUsuario)
            }
        } catch (e: ErrorRed) {
            ResultadoSalud.Fallida(e.mensajeUsuario)
        }

    companion object {
        const val RUTA_SALUD = "/api/health"
        const val MENSAJE_BD_CAIDA = "El servidor responde, pero la base de datos no"
    }
}
