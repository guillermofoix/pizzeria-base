package es.fpmola.pizzeria.pagos

/** Primera frase del aviso cuando el pedido se crea pero no se puede iniciar el pago. */
const val AVISO_PAGO_NO_INICIADO = "Tu pedido está creado, pero no hemos podido iniciar el pago."

/** Resultado de iniciar el pago con tarjeta de un pedido ya creado. */
sealed interface ResultadoInicioPago {

    /** Se creó la sesión de pago y se abrió su página en el navegador. */
    data object Iniciado : ResultadoInicioPago

    /** No se pudo iniciar el pago; [mensaje] explica el motivo en español. */
    data class Fallido(val mensaje: String) : ResultadoInicioPago
}

/**
 * Inicia el pago con tarjeta de un pedido que ya existe: pide una sesión nueva
 * al servidor y abre su URL en el navegador. Nunca crea ni modifica pedidos.
 *
 * La URL solo se abre si [abridor] la acepta (https de Stripe).
 */
suspend fun iniciarPagoConTarjeta(
    repositorio: RepositorioPagos,
    abridor: AbridorUrl,
    pedidoId: Int,
): ResultadoInicioPago =
    when (val sesion = repositorio.crearSesion(pedidoId)) {
        is ResultadoSesionPago.Fallida -> ResultadoInicioPago.Fallido(sesion.error.mensajeUsuario)
        is ResultadoSesionPago.Creada -> when (val abierta = abridor.abrir(sesion.url)) {
            ResultadoAbrirUrl.Abierta -> ResultadoInicioPago.Iniciado
            is ResultadoAbrirUrl.Fallida -> ResultadoInicioPago.Fallido(abierta.mensaje)
        }
    }
