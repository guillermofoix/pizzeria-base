package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.pedidos.DatosPedido
import es.fpmola.pizzeria.pedidos.ErroresDatos
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.PeticionPedido
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.pedidos.ResultadoEnvioPedido
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.pedidos.construirPeticion
import es.fpmola.pizzeria.pedidos.mensajeEnvioIncierto
import es.fpmola.pizzeria.pedidos.validarDatos
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de datos del pedido.
 *
 * @property enviando true mientras se envía el pedido (el botón queda desactivado).
 * @property mensajeError motivo por el que no se pudo enviar el pedido.
 * @property envioIncierto true si no se sabe si el pedido llegó al servidor.
 * @property cartaPorRefrescar true si el servidor rechazó el pedido y hay que
 * volver a cargar la carta (por si una pizza ya no es válida).
 * @property pedidoCreado el pedido que creó el servidor, cuando se envió bien.
 */
data class EstadoDatosPedido(
    val datos: DatosPedido = DatosPedido(),
    val errores: ErroresDatos = ErroresDatos(),
    val enviando: Boolean = false,
    val mensajeError: String? = null,
    val envioIncierto: Boolean = false,
    val cartaPorRefrescar: Boolean = false,
    val pedidoCreado: Pedido? = null,
)

/**
 * Lógica de la pantalla de datos del pedido: guarda lo que escribe el usuario,
 * lo valida según el tipo de pedido y envía el pedido.
 *
 * Reglas del envío:
 * - Sin doble envío: mientras se envía, o una vez creado el pedido, no se
 *   vuelve a enviar.
 * - Nunca se reintenta automáticamente.
 * - Si falla por red o por un error del servidor, el carrito se mantiene y se
 *   avisa de que el pedido quizá sí llegó.
 * - Si el pedido se crea, el carrito se vacía.
 */
class DatosPedidoViewModel(
    private val repositorio: RepositorioPedidos,
    private val carrito: Carrito,
) : ViewModel() {

    var estado by mutableStateOf(EstadoDatosPedido())
        private set

    fun cambiarTipo(tipo: TipoPedido) =
        actualizar(reiniciarErrores = true) { it.copy(tipo = tipo) }

    fun cambiarMesa(texto: String) = actualizar { it.copy(mesa = texto) }

    fun cambiarNombre(texto: String) = actualizar { it.copy(nombre = texto) }

    fun cambiarTelefono(texto: String) = actualizar { it.copy(telefono = texto) }

    fun cambiarDireccion(texto: String) = actualizar { it.copy(direccion = texto) }

    fun cambiarObservaciones(texto: String) = actualizar { it.copy(observaciones = texto) }

    /**
     * Valida los datos y guarda los errores en el estado.
     *
     * @return true si los datos son correctos.
     */
    fun validar(): Boolean {
        val errores = validarDatos(estado.datos)
        estado = estado.copy(errores = errores)
        return !errores.hayErrores
    }

    /**
     * Envía el pedido desde la interfaz. Corre en el ámbito del ViewModel para
     * que un giro de pantalla o salir de ella no cancele la petición y se pierda
     * la respuesta.
     */
    fun enviar() {
        val peticion = prepararEnvio() ?: return
        viewModelScope.launch { completarEnvio(peticion) }
    }

    /** Igual que [enviar], pero espera a que termine (para los tests). */
    suspend fun enviarPedido() {
        val peticion = prepararEnvio() ?: return
        completarEnvio(peticion)
    }

    /** La pantalla ya ha vuelto a cargar la carta tras un rechazo del servidor. */
    fun cartaRefrescada() {
        estado = estado.copy(cartaPorRefrescar = false)
    }

    /**
     * Comprueba que se puede enviar y marca "enviando" de forma inmediata (así
     * un segundo toque no envía otra vez). Devuelve la petición, o null si no
     * hay que enviar nada.
     */
    private fun prepararEnvio(): PeticionPedido? {
        if (estado.enviando || estado.pedidoCreado != null) return null
        if (!validar()) return null

        val lineas = carrito.lineas
        if (lineas.isEmpty()) {
            estado = estado.copy(mensajeError = "El carrito está vacío.")
            return null
        }

        estado = estado.copy(
            enviando = true,
            mensajeError = null,
            envioIncierto = false,
            cartaPorRefrescar = false,
        )
        return construirPeticion(estado.datos, lineas)
    }

    private suspend fun completarEnvio(peticion: PeticionPedido) {
        val resultado = try {
            repositorio.crearPedido(peticion)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Salvaguarda: que "enviando" no se quede activo por un fallo imprevisto.
            ResultadoEnvioPedido.Incierto(mensajeEnvioIncierto("Ha ocurrido un error inesperado."))
        }

        estado = when (resultado) {
            is ResultadoEnvioPedido.Creado -> {
                carrito.vaciar()
                estado.copy(enviando = false, pedidoCreado = resultado.pedido)
            }
            is ResultadoEnvioPedido.Rechazado -> estado.copy(
                enviando = false,
                mensajeError = resultado.mensaje + " " + AVISO_CARTA_ACTUALIZADA,
                cartaPorRefrescar = true,
            )
            is ResultadoEnvioPedido.Incierto -> estado.copy(
                enviando = false,
                mensajeError = resultado.mensaje,
                envioIncierto = true,
            )
            is ResultadoEnvioPedido.NoEnviado -> estado.copy(
                enviando = false,
                mensajeError = resultado.mensaje,
            )
        }
    }

    /**
     * Cambia los datos. Si ya se mostraron errores, se vuelven a calcular al
     * escribir para que desaparezcan en cuanto el campo es correcto.
     */
    private fun actualizar(
        reiniciarErrores: Boolean = false,
        cambio: (DatosPedido) -> DatosPedido,
    ) {
        val datos = cambio(estado.datos)
        val errores = when {
            reiniciarErrores -> ErroresDatos()
            estado.errores.hayErrores -> validarDatos(datos)
            else -> estado.errores
        }
        estado = estado.copy(datos = datos, errores = errores, mensajeError = null)
    }

    private companion object {
        const val AVISO_CARTA_ACTUALIZADA =
            "Hemos actualizado la carta por si algún producto ya no está disponible."
    }
}
