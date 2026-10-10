package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.red.ErrorRed
import kotlinx.coroutines.delay

/**
 * Estado de la pantalla de pedido confirmado.
 *
 * @property pedido el último pedido conocido (el de la creación hasta que llega
 * la primera consulta).
 * @property error motivo por el que falló la última actualización, si falló.
 */
data class EstadoPedidoConfirmado(
    val pedido: Pedido? = null,
    val actualizando: Boolean = false,
    val error: String? = null,
)

/**
 * Seguimiento de un pedido ya creado: consulta GET /api/pedidos/{id} una vez
 * ([actualizar]) o cada pocos segundos hasta que el estado es final ([sondear]).
 */
class PedidoConfirmadoViewModel(
    private val repositorio: RepositorioPedidos,
    private val pedidoId: Int,
    pedidoInicial: Pedido? = null,
) : ViewModel() {

    var estado by mutableStateOf(EstadoPedidoConfirmado(pedido = pedidoInicial))
        private set

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
     */
    suspend fun sondear(intervaloMs: Long = INTERVALO_SONDEO_MS) {
        while (true) {
            actualizar()
            if (estado.pedido?.esFinal == true) return
            delay(intervaloMs)
        }
    }

    companion object {
        /** Tiempo entre consultas: "cada pocos segundos". */
        const val INTERVALO_SONDEO_MS = 5_000L
    }
}
