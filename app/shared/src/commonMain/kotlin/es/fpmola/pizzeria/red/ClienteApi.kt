package es.fpmola.pizzeria.red

import es.fpmola.pizzeria.ajustes.AjustesServidor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json

/** Configuración JSON común: tolera campos nuevos que añada el backend. */
val jsonApi: Json = Json {
    ignoreUnknownKeys = true
}

/**
 * Cliente HTTP del backend.
 *
 * La URL base se lee de [AjustesServidor] en cada petición, así que un cambio
 * en la configuración se aplica sin reiniciar la app. Los fallos se convierten
 * siempre en [ErrorRed].
 *
 * @param motor motor de Ktor (OkHttp en Android, MockEngine en los tests).
 */
class ClienteApi(
    private val ajustes: AjustesServidor,
    motor: HttpClientEngine,
) {
    private val http = HttpClient(motor) {
        // Los códigos de error se tratan a mano para tiparlos.
        expectSuccess = false
        install(ContentNegotiation) {
            json(jsonApi)
        }
    }

    /**
     * Hace un GET a [ruta] (por ejemplo "/api/health") y devuelve la respuesta
     * sin comprobar el código HTTP.
     *
     * @param urlBase URL alternativa a la guardada (para probar una URL nueva).
     * @throws ErrorRed.SinServidorConfigurado si no hay URL disponible.
     * @throws ErrorRed.SinConexion si la petición no llega a completarse.
     */
    suspend fun peticionGet(ruta: String, urlBase: String? = null): HttpResponse {
        val base = urlBase ?: ajustes.obtenerUrlBase() ?: throw ErrorRed.SinServidorConfigurado()
        return try {
            http.get(base + ruta)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ErrorRed.SinConexion(e)
        }
    }

    /**
     * Hace un POST a [ruta] con un cuerpo JSON ya serializado y devuelve la
     * respuesta sin comprobar el código HTTP.
     *
     * No reintenta nunca: un POST puede crear algo en el servidor, así que
     * repetirlo podría duplicarlo. Quien llama decide qué hacer ante un fallo.
     *
     * @throws ErrorRed.SinServidorConfigurado si no hay URL guardada.
     * @throws ErrorRed.SinConexion si la petición no llega a completarse (en
     * ese caso no se sabe si el servidor llegó a recibirla).
     */
    suspend fun peticionPostJson(ruta: String, cuerpoJson: String): HttpResponse {
        val base = ajustes.obtenerUrlBase() ?: throw ErrorRed.SinServidorConfigurado()
        return try {
            http.post(base + ruta) {
                setBody(TextContent(cuerpoJson, ContentType.Application.Json))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ErrorRed.SinConexion(e)
        }
    }

    /**
     * GET que exige un código 2xx y convierte el cuerpo JSON en [T].
     *
     * @throws ErrorRed.Servidor si el código HTTP no es 2xx.
     * @throws ErrorRed.RespuestaInvalida si el cuerpo no se puede leer como [T].
     */
    suspend inline fun <reified T> obtener(ruta: String, urlBase: String? = null): T {
        val respuesta = peticionGet(ruta, urlBase)
        if (!respuesta.status.isSuccess()) {
            throw ErrorRed.Servidor(respuesta.status.value)
        }
        return respuesta.leerCuerpo()
    }
}

/**
 * Lee el cuerpo JSON de la respuesta como [T].
 *
 * @throws ErrorRed.RespuestaInvalida si el cuerpo no es JSON válido para [T].
 */
suspend inline fun <reified T> HttpResponse.leerCuerpo(): T =
    try {
        body<T>()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw ErrorRed.RespuestaInvalida(e)
    }
