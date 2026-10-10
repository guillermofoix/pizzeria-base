package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.fpmola.pizzeria.pagos.AbridorUrl
import es.fpmola.pizzeria.pagos.RepositorioPagos
import es.fpmola.pizzeria.pagos.ResultadoConfirmacionPago
import es.fpmola.pizzeria.pagos.ResultadoInicioPago
import es.fpmola.pizzeria.pagos.iniciarPagoConTarjeta
import es.fpmola.pizzeria.pedidos.ESTADO_PAGO_PAGADO
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.red.ErrorRed
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay

/**
 * Estado de la pantalla de pedido confirmado.
 *
 * @property pedido el último pedido conocido (el de la creación hasta que llega
 * la primera consulta).
 * @property error motivo por el que falló la última actualización, si falló.
 * @property procesandoPago true mientras se abre el pago o se comprueba (los
 * botones de pago quedan desactivados).
 * @property errorPago aviso de que el pago no se pudo iniciar o comprobar.
 * @property infoPago mensaje informativo sobre el pago (por ejemplo, que aún no consta).
 * @property esperaPagoAgotada true si el sondeo se detuvo tras esperar el pago
 * demasiado tiempo: a partir de ahí solo se actualiza con el botón "Actualizar".
 */
data class EstadoPedidoConfirmado(
    val pedido: Pedido? = null,
    val actualizando: Boolean = false,
    val error: String? = null,
    val procesandoPago: Boolean = false,
    val errorPago: String? = null,
    val infoPago: String? = null,
    val esperaPagoAgotada: Boolean = false,
)

/**
 * Seguimiento de un pedido ya creado: consulta GET /api/pedidos/{id} una vez
 * ([actualizar]) o cada pocos segundos hasta que el estado es final ([sondear]).
 *
 * Si el pedido se paga con tarjeta, también permite abrir el pago de nuevo
 * ([pagarConTarjeta]) y preguntar al servidor si ya consta ([comprobarPago]).
 * El estado del pago lo decide siempre el servidor.
 *
 * @param errorPagoInicial aviso con el que se abre la pantalla cuando el pedido
 * se creó pero no se pudo iniciar el pago.
 */
class PedidoConfirmadoViewModel(
    private val repositorio: RepositorioPedidos,
    private val repositorioPagos: RepositorioPagos,
    private val abridor: AbridorUrl,
    private val pedidoId: Int,
    pedidoInicial: Pedido? = null,
    errorPagoInicial: String? = null,
) : ViewModel() {

    var estado by mutableStateOf(
        EstadoPedidoConfirmado(pedido = pedidoInicial, errorPago = errorPagoInicial),
    )
        private set

    /**
     * Pide una sesión de pago nueva (el backend lo permite mientras el pedido
     * no esté pagado) y abre su URL en el navegador. Si ya hay una operación de
     * pago en curso, no hace nada.
     */
    suspend fun pagarConTarjeta() {
        if (estado.procesandoPago) return
        estado = estado.copy(procesandoPago = true, errorPago = null, infoPago = null)
        try {
            val resultado = try {
                iniciarPagoConTarjeta(repositorioPagos, abridor, pedidoId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ResultadoInicioPago.Fallido("Ha ocurrido un error inesperado.")
            }
            estado = when (resultado) {
                ResultadoInicioPago.Iniciado -> estado.copy(
                    infoPago = "Hemos abierto la página de pago en el navegador.",
                )
                is ResultadoInicioPago.Fallido -> estado.copy(errorPago = resultado.mensaje)
            }
        } finally {
            estado = estado.copy(procesandoPago = false)
        }
    }

    /**
     * Pregunta al servidor si el pedido ya está pagado (confirmar-sesion con
     * SOLO el id del pedido) y actualiza el pedido. Solo se llama cuando el
     * usuario pulsa el botón: nunca de forma automática.
     */
    suspend fun comprobarPago() {
        if (estado.procesandoPago) return
        estado = estado.copy(procesandoPago = true, errorPago = null, infoPago = null)
        try {
            val resultado = try {
                repositorioPagos.confirmarPago(pedidoId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            when (resultado) {
                ResultadoConfirmacionPago.Pagado -> {
                    // Se refleja ya el pago y se refresca el pedido completo.
                    estado = estado.copy(pedido = estado.pedido?.copy(estadoPago = ESTADO_PAGO_PAGADO))
                    actualizar()
                }
                is ResultadoConfirmacionPago.TodaviaSinPagar -> estado = estado.copy(
                    infoPago = "Todavía no consta el pago. Si acabas de pagar, espera unos segundos " +
                        "y vuelve a comprobarlo.",
                )
                is ResultadoConfirmacionPago.Fallida -> estado = estado.copy(
                    errorPago = resultado.error.mensajeUsuario,
                )
                null -> estado = estado.copy(errorPago = "Ha ocurrido un error inesperado.")
            }
        } finally {
            estado = estado.copy(procesandoPago = false)
        }
    }

    /**
     * Consulta el pedido una vez. Si ya hay una consulta en curso, no hace nada.
     * Un fallo se guarda en el estado y se conserva el último pedido conocido.
     */
    suspend fun actualizar() {
        if (estado.actualizando) return
        estado = estado.copy(actualizando = true)
        try {
            val pedido = repositorio.obtenerPedido(pedidoId)
            estado = estado.copy(pedido = pedido, error = null)
        } catch (e: ErrorRed) {
            estado = estado.copy(error = e.mensajeUsuario)
        } finally {
            estado = estado.copy(actualizando = false)
        }
    }

    /**
     * Consulta el pedido cada [intervaloMs] milisegundos y termina cuando su
     * estado es final (entregado, servido o cancelado). Es cancelable: se
     * detiene sola cuando la pantalla que la lanza sale de la composición.
     * Un fallo de red no la detiene: se vuelve a intentar en la siguiente vuelta.
     *
     * Si el pedido es con tarjeta y el pago sigue pendiente, NO se detiene en un
     * estado final: sigue hasta que el servidor marque el pago como pagado.
     * Para no consultar sin fin, se detiene tras [limitePagoMs] de espera (se
     * cuenta con los intervalos) y marca [EstadoPedidoConfirmado.esperaPagoAgotada].
     * Nunca llama a confirmar-sesion: solo lee el pedido.
     */
    suspend fun sondear(
        intervaloMs: Long = INTERVALO_SONDEO_MS,
        limitePagoMs: Long = LIMITE_ESPERA_PAGO_MS,
    ) {
        estado = estado.copy(esperaPagoAgotada = false)
        var esperadoMs = 0L
        while (true) {
            actualizar()
            val pedido = estado.pedido
            val pagoPendiente = pedido?.pagoConTarjetaPendiente == true
            if (pedido?.esFinal == true && !pagoPendiente) return
            if (pagoPendiente && esperadoMs >= limitePagoMs) {
                estado = estado.copy(esperaPagoAgotada = true)
                return
            }
            delay(intervaloMs)
            esperadoMs += intervaloMs
        }
    }

    companion object {
        /** Tiempo entre consultas: "cada pocos segundos". */
        const val INTERVALO_SONDEO_MS = 5_000L

        /** Tiempo máximo esperando un pago con tarjeta: 15 minutos. */
        const val LIMITE_ESPERA_PAGO_MS = 15 * 60_000L
    }
}
