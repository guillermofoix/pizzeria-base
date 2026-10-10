package es.fpmola.pizzeria.pagos

import es.fpmola.pizzeria.pedidos.ESTADO_PAGO_PAGADO
import es.fpmola.pizzeria.red.ClienteApi
import es.fpmola.pizzeria.red.ErrorRed
import es.fpmola.pizzeria.red.jsonApi
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.KSerializer

/**
 * Pago con tarjeta (Stripe Checkout) a través del backend. La app no lleva
 * ninguna clave ni librería de Stripe: pide una sesión al servidor y el estado
 * del pago lo decide el servidor (webhook de Stripe).
 *
 * Ninguna operación lanza [ErrorRed]: todos los desenlaces se devuelven como
 * resultado, con mensajes en español. Nada se reintenta automáticamente.
 */
class RepositorioPagos(private val cliente: ClienteApi) {

    /**
     * Pide una sesión de pago para un pedido que ya existe
     * (POST /api/pagos/crear-sesion). Cada llamada crea una sesión nueva; el
     * backend lo permite mientras el pedido no esté pagado.
     */
    suspend fun crearSesion(pedidoId: Int): ResultadoSesionPago {
        val cuerpo = jsonApi.encodeToString(PeticionSesionPago.serializer(), PeticionSesionPago(pedidoId))
        val respuesta = try {
            cliente.peticionPostJson(RUTA_CREAR_SESION, cuerpo)
        } catch (e: ErrorRed) {
            return ResultadoSesionPago.Fallida(errorDeRed(e))
        }

        val datos = leerRespuesta(respuesta, RespuestaSesionPago.serializer())
        if (!respuesta.status.isSuccess()) {
            return ResultadoSesionPago.Fallida(errorDeCodigo(respuesta.status.value, datos?.message))
        }
        val url = datos?.url?.takeIf { it.isNotBlank() }
        return if (datos != null && datos.success && url != null) {
            ResultadoSesionPago.Creada(url = url, idSesion = datos.idSesion)
        } else {
            ResultadoSesionPago.Fallida(ErrorPago.Otro(MENSAJE_RESPUESTA_ILEGIBLE))
        }
    }

    /**
     * Pregunta al servidor si el pedido ya está pagado
     * (POST /api/pagos/confirmar-sesion). Envía SOLAMENTE `pedido_id`, nunca
     * `session_id`: así el servidor usa la sesión guardada en ese pedido.
     */
    suspend fun confirmarPago(pedidoId: Int): ResultadoConfirmacionPago {
        val cuerpo = jsonApi.encodeToString(PeticionConfirmarPago.serializer(), PeticionConfirmarPago(pedidoId))
        val respuesta = try {
            cliente.peticionPostJson(RUTA_CONFIRMAR_SESION, cuerpo)
        } catch (e: ErrorRed) {
            return ResultadoConfirmacionPago.Fallida(errorDeRed(e))
        }

        val datos = leerRespuesta(respuesta, RespuestaConfirmarPago.serializer())
        if (!respuesta.status.isSuccess()) {
            return ResultadoConfirmacionPago.Fallida(errorDeCodigo(respuesta.status.value, datos?.message))
        }
        val estadoPago = if (datos != null && datos.success) datos.estadoPago else null
        return when {
            estadoPago == null -> ResultadoConfirmacionPago.Fallida(ErrorPago.Otro(MENSAJE_RESPUESTA_ILEGIBLE))
            estadoPago == ESTADO_PAGO_PAGADO -> ResultadoConfirmacionPago.Pagado
            else -> ResultadoConfirmacionPago.TodaviaSinPagar(estadoPago)
        }
    }

    private fun errorDeRed(e: ErrorRed): ErrorPago = when (e) {
        is ErrorRed.SinConexion -> ErrorPago.SinConexion
        else -> ErrorPago.Otro(e.mensajeUsuario)
    }

    /**
     * Traduce un código HTTP de error. El backend responde 400 (sin pedido_id,
     * pedido ya pagado o sin líneas), 404 (pedido inexistente) y 503 (Stripe
     * no configurado). El 400 "ya pagado" solo se distingue por su mensaje.
     */
    private fun errorDeCodigo(codigo: Int, mensaje: String?): ErrorPago = when {
        codigo == 503 -> ErrorPago.PagoNoDisponible
        codigo == 400 && mensaje?.contains("ya fue pagado") == true -> ErrorPago.PedidoYaPagado
        else -> ErrorPago.Otro(
            mensaje?.takeIf { it.isNotBlank() }
                ?: "El servidor no ha podido completar la operación de pago (código $codigo).",
        )
    }

    /** El cuerpo de la respuesta como [T], o null si no es JSON válido para [T]. */
    private suspend fun <T> leerRespuesta(respuesta: HttpResponse, serializador: KSerializer<T>): T? =
        try {
            jsonApi.decodeFromString(serializador, respuesta.bodyAsText())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    companion object {
        const val RUTA_CREAR_SESION = "/api/pagos/crear-sesion"
        const val RUTA_CONFIRMAR_SESION = "/api/pagos/confirmar-sesion"
        private const val MENSAJE_RESPUESTA_ILEGIBLE =
            "El servidor ha enviado una respuesta que la app no entiende."
    }
}
