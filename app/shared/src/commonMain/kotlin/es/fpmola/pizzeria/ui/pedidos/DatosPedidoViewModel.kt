package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.fpmola.pizzeria.pedidos.DatosPedido
import es.fpmola.pizzeria.pedidos.ErroresDatos
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.pedidos.validarDatos

/**
 * Estado de la pantalla de datos del pedido.
 *
 * @property enviando true mientras se envía el pedido (el botón queda desactivado).
 * @property mensajeError motivo por el que no se pudo enviar el pedido.
 */
data class EstadoDatosPedido(
    val datos: DatosPedido = DatosPedido(),
    val errores: ErroresDatos = ErroresDatos(),
    val enviando: Boolean = false,
    val mensajeError: String? = null,
)

/**
 * Lógica de la pantalla de datos del pedido: guarda lo que escribe el usuario
 * y lo valida según el tipo de pedido.
 */
class DatosPedidoViewModel : ViewModel() {

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
}
