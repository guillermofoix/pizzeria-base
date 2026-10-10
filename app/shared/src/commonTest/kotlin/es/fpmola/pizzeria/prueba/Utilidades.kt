package es.fpmola.pizzeria.prueba

import es.fpmola.pizzeria.ajustes.AjustesServidor
import es.fpmola.pizzeria.red.ClienteApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** URL base usada en los tests (nunca se contacta: el motor es simulado). */
const val URL_PRUEBA = "https://servidor.prueba"

/** AjustesServidor en memoria para los tests. */
class AjustesEnMemoria(var url: String? = URL_PRUEBA) : AjustesServidor {
    override fun obtenerUrlBase(): String? = url
    override fun guardarUrlBase(url: String) {
        this.url = url
    }
}

/** Responde con un cuerpo JSON y el código indicado. */
fun MockRequestHandleScope.responderJson(
    cuerpo: String,
    estado: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = cuerpo,
    status = estado,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)

/** Crea un ClienteApi que usa el motor simulado de Ktor en lugar de la red. */
fun crearClientePrueba(
    ajustes: AjustesServidor = AjustesEnMemoria(),
    motor: MockEngine,
): ClienteApi = ClienteApi(ajustes, motor)
