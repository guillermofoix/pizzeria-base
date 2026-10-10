package es.fpmola.pizzeria.ui.catalogo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.fpmola.pizzeria.catalogo.Categoria
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.catalogo.RepositorioCatalogo
import es.fpmola.pizzeria.red.ErrorRed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Estado de la pantalla de catálogo.
 *
 * @property categoriaSeleccionadaId id de la categoría elegida; null es "Todas".
 */
data class EstadoCatalogo(
    val cargando: Boolean = true,
    val cargada: Boolean = false,
    val error: String? = null,
    val pizzas: List<Pizza> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionadaId: Int? = null,
) {
    /** Hay que mostrar la rueda de carga: cargando, o aún sin datos ni error. */
    val enCarga: Boolean
        get() = cargando || (!cargada && error == null)

    /** Pizzas de la categoría elegida (todas si no hay ninguna elegida). */
    val pizzasFiltradas: List<Pizza>
        get() = if (categoriaSeleccionadaId == null) {
            pizzas
        } else {
            pizzas.filter { it.categoriaId == categoriaSeleccionadaId }
        }
}

/**
 * Lógica de la pantalla de catálogo: cargar la carta y las categorías y
 * filtrar por categoría.
 *
 * La carga es una función `suspend` que ejecuta quien la llama (la pantalla,
 * con un efecto de Compose), así que se cancela sola al salir de la pantalla.
 */
class CatalogoViewModel(
    private val repositorio: RepositorioCatalogo,
) : ViewModel() {

    var estado by mutableStateOf(EstadoCatalogo())
        private set

    /** Carga los datos solo si todavía no se han cargado correctamente. */
    suspend fun cargarSiHaceFalta() {
        if (!estado.cargada) cargar()
    }

    /**
     * Pide la carta y las categorías al servidor (en paralelo). Si algo falla
     * guarda el mensaje de error en español en el estado.
     */
    suspend fun cargar() {
        estado = estado.copy(cargando = true, error = null)
        try {
            val (pizzas, metadatos) = coroutineScope {
                val pizzasPedidas = async { repositorio.obtenerPizzas() }
                val metadatosPedidos = async { repositorio.obtenerMetadatos() }
                pizzasPedidas.await() to metadatosPedidos.await()
            }
            val categorias = metadatos.categorias
            val seleccion = estado.categoriaSeleccionadaId
                ?.takeIf { id -> categorias.any { it.id == id } }
            estado = estado.copy(
                cargando = false,
                cargada = true,
                pizzas = pizzas,
                categorias = categorias,
                categoriaSeleccionadaId = seleccion,
            )
        } catch (e: ErrorRed) {
            estado = estado.copy(cargando = false, error = e.mensajeUsuario)
        } finally {
            // Si la carga se canceló (se salió de la pantalla), no dejar "cargando".
            if (estado.cargando) estado = estado.copy(cargando = false)
        }
    }

    /** Filtra por categoría; null muestra todas las pizzas. */
    fun seleccionarCategoria(categoriaId: Int?) {
        estado = estado.copy(categoriaSeleccionadaId = categoriaId)
    }
}
