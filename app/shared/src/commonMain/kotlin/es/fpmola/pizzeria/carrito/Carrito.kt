package es.fpmola.pizzeria.carrito

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.modelo.Importe

/**
 * Línea del carrito: una pizza de la carta y cuántas unidades se quieren.
 *
 * Guarda una copia de los datos que necesita la interfaz. El id siempre viene
 * de una [Pizza] de la carta cargada, nunca se escribe a mano.
 */
data class LineaCarrito(
    val pizzaId: Int,
    val nombre: String,
    val imagenUrl: String?,
    val precioUnitario: Importe,
    val cantidad: Int,
) {
    /** Precio de la línea: precio unitario por unidades. */
    val subtotal: Importe
        get() = precioUnitario * cantidad

    companion object {
        fun desde(pizza: Pizza, cantidad: Int): LineaCarrito = LineaCarrito(
            pizzaId = pizza.id,
            nombre = pizza.nombre,
            imagenUrl = pizza.imagenUrl,
            precioUnitario = pizza.precio,
            cantidad = cantidad,
        )
    }
}

/** Resultado de intentar añadir una pizza al carrito. */
enum class ResultadoAnadir {
    /** Se añadieron todas las unidades pedidas. */
    Anadida,

    /** Se llegó al máximo por pizza: se añadieron solo las unidades que cabían. */
    LimiteAlcanzado,

    /** La pizza está agotada: no se añade. */
    NoDisponible,
}

/**
 * Carrito de la compra, compartido por todas las pantallas (se obtiene con Koin).
 * Vive solo en memoria: se pierde al cerrar la app.
 *
 * El precio de cada línea es el de la carta cuando se añadió; el total es solo
 * una estimación, porque el servidor recalcula los precios al crear el pedido.
 */
class Carrito {

    /** Líneas del carrito, en el orden en que se añadieron. */
    var lineas: List<LineaCarrito> by mutableStateOf(emptyList())
        private set

    /** Pizzas quitadas al ajustar el carrito a la carta (para avisar al usuario). */
    var quitadasPorCarta: List<String> by mutableStateOf(emptyList())
        private set

    val estaVacio: Boolean
        get() = lineas.isEmpty()

    /** Número total de unidades (suma de las cantidades de todas las líneas). */
    val totalUnidades: Int
        get() = lineas.sumOf { it.cantidad }

    /** Total estimado del carrito. */
    val total: Importe
        get() = lineas.fold(Importe.CERO) { acumulado, linea -> acumulado + linea.subtotal }

    /** Unidades de una pizza en el carrito (0 si no está). */
    fun cantidadDe(pizzaId: Int): Int =
        lineas.firstOrNull { it.pizzaId == pizzaId }?.cantidad ?: 0

    /**
     * Añade [cantidad] unidades de [pizza]. Si ya estaba, las suma, sin pasar
     * de [CANTIDAD_MAXIMA]. Una pizza agotada no se añade.
     */
    fun anadir(pizza: Pizza, cantidad: Int = 1): ResultadoAnadir {
        if (!pizza.disponible) return ResultadoAnadir.NoDisponible

        val pedida = cantidad.coerceIn(CANTIDAD_MINIMA, CANTIDAD_MAXIMA)
        val actual = cantidadDe(pizza.id)
        val nueva = (actual + pedida).coerceAtMost(CANTIDAD_MAXIMA)
        val lineaNueva = LineaCarrito.desde(pizza, nueva)

        lineas = if (actual == 0) {
            lineas + lineaNueva
        } else {
            lineas.map { if (it.pizzaId == pizza.id) lineaNueva else it }
        }

        // Se compara la cantidad pedida sin recortar, para avisar también si
        // pedía más de las que caben.
        return if (cantidad > CANTIDAD_MAXIMA - actual) {
            ResultadoAnadir.LimiteAlcanzado
        } else {
            ResultadoAnadir.Anadida
        }
    }

    /**
     * Fija la cantidad de una línea, entre [CANTIDAD_MINIMA] y [CANTIDAD_MAXIMA].
     * Para quitar la línea hay que usar [quitar]. Si la pizza no está, no hace nada.
     */
    fun cambiarCantidad(pizzaId: Int, cantidad: Int) {
        val ajustada = cantidad.coerceIn(CANTIDAD_MINIMA, CANTIDAD_MAXIMA)
        lineas = lineas.map { if (it.pizzaId == pizzaId) it.copy(cantidad = ajustada) else it }
    }

    /** Quita la línea de una pizza. */
    fun quitar(pizzaId: Int) {
        lineas = lineas.filterNot { it.pizzaId == pizzaId }
    }

    /** Vacía el carrito. */
    fun vaciar() {
        lineas = emptyList()
    }

    /**
     * Ajusta el carrito a la carta recién cargada: quita las pizzas que ya no
     * existen o están agotadas y actualiza el nombre y el precio del resto.
     * Los nombres de las pizzas quitadas quedan en [quitadasPorCarta].
     *
     * @return nombres de las pizzas quitadas.
     */
    fun sincronizarConCarta(pizzas: List<Pizza>): List<String> {
        val porId = pizzas.associateBy { it.id }
        val quitadas = mutableListOf<String>()
        val ajustadas = lineas.mapNotNull { linea ->
            val pizza = porId[linea.pizzaId]
            if (pizza == null || !pizza.disponible) {
                quitadas += linea.nombre
                null
            } else {
                LineaCarrito.desde(pizza, linea.cantidad)
            }
        }
        if (ajustadas != lineas) lineas = ajustadas
        if (quitadas.isNotEmpty()) quitadasPorCarta = quitadas
        return quitadas
    }

    /** El usuario ya ha visto el aviso de pizzas quitadas. */
    fun descartarAviso() {
        quitadasPorCarta = emptyList()
    }

    companion object {
        const val CANTIDAD_MINIMA = 1
        const val CANTIDAD_MAXIMA = 20
    }
}
