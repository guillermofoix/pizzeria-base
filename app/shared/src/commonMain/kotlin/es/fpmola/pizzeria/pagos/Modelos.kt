package es.fpmola.pizzeria.pagos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Cuerpo de POST /api/pagos/crear-sesion. */
@Serializable
data class PeticionSesionPago(
    @SerialName("pedido_id") val pedidoId: Int,
)

/**
 * Respuesta de POST /api/pagos/crear-sesion. NO usa el envoltorio
 * {success, data}: `url` y `sessionId` vienen al mismo nivel que `success`:
 * {"success":true,"url":"https://checkout.stripe.com/…","sessionId":"cs_…"}
 *
 * Los errores (400, 404, 503) traen `success` en false y un `message`.
 *
 * @property idSesion identificador de la sesión de Stripe (`sessionId` en la API).
 */
@Serializable
data class RespuestaSesionPago(
    val success: Boolean = false,
    val url: String? = null,
    @SerialName("sessionId") val idSesion: String? = null,
    val message: String? = null,
)

/**
 * Cuerpo de POST /api/pagos/confirmar-sesion. Lleva SOLO el id del pedido:
 * si se enviara también `session_id`, el servidor marcaría como pagado el
 * pedido del cuerpo aunque la sesión fuera de otro (fallo conocido del backend).
 */
@Serializable
data class PeticionConfirmarPago(
    @SerialName("pedido_id") val pedidoId: Int,
)

/**
 * Respuesta de POST /api/pagos/confirmar-sesion. Tampoco usa el envoltorio:
 * {"success":true,"estado_pago":"pagado","pedido_id":104} o, si Stripe aún no
 * ha cobrado, {"success":true,"estado_pago":"unpaid","message":"…"}.
 */
@Serializable
data class RespuestaConfirmarPago(
    val success: Boolean = false,
    @SerialName("estado_pago") val estadoPago: String? = null,
    val message: String? = null,
)
