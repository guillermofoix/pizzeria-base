package es.fpmola.pizzeria.ui.catalogo

import es.fpmola.pizzeria.catalogo.RepositorioCatalogo
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_METADATOS
import es.fpmola.pizzeria.prueba.JSON_PIZZAS
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CatalogoViewModelTest {

    /** Motor que sirve la carta y los metadatos, o falla con 500 si [caido]. */
    private class ServidorSimulado {
        var caido = false
        var peticionesPizzas = 0
        var peticionesMetadatos = 0

        val motor = MockEngine { peticion ->
            val esMetadatos = peticion.url.encodedPath.endsWith("/metadata")
            // Cada ruta tiene su contador: las dos peticiones se hacen en paralelo.
            if (esMetadatos) peticionesMetadatos++ else peticionesPizzas++
            when {
                caido -> responderJson(
                    """{"success":false,"message":"Error interno"}""",
                    HttpStatusCode.InternalServerError,
                )
                esMetadatos -> responderJson(JSON_METADATOS)
                else -> responderJson(JSON_PIZZAS)
            }
        }
    }

    private fun crearViewModel(servidor: ServidorSimulado) =
        CatalogoViewModel(RepositorioCatalogo(crearClientePrueba(AjustesEnMemoria(), servidor.motor)))

    private fun idsFiltrados(modelo: CatalogoViewModel, categoriaId: Int?): List<Int> {
        modelo.seleccionarCategoria(categoriaId)
        return modelo.estado.pizzasFiltradas.map { it.id }
    }

    // --- Carga ---------------------------------------------------------------

    @Test
    fun alEmpezarEstaEnCarga() {
        val estado = crearViewModel(ServidorSimulado()).estado

        assertTrue(estado.enCarga)
        assertNull(estado.error)
        assertTrue(estado.pizzas.isEmpty())
    }

    @Test
    fun cargaLaCartaYLasCategorias() = runTest {
        val modelo = crearViewModel(ServidorSimulado())

        modelo.cargar()

        val estado = modelo.estado
        assertFalse(estado.enCarga)
        assertTrue(estado.cargada)
        assertNull(estado.error)
        assertEquals(4, estado.pizzas.size)
        assertEquals(4, estado.categorias.size)
        assertNull(estado.categoriaSeleccionadaId)
    }

    @Test
    fun siElServidorFallaGuardaElMensajeEnEspanol() = runTest {
        val servidor = ServidorSimulado().apply { caido = true }
        val modelo = crearViewModel(servidor)

        modelo.cargar()

        val estado = modelo.estado
        assertFalse(estado.enCarga)
        assertFalse(estado.cargada)
        assertEquals(ErrorRed.Servidor(500).mensajeUsuario, estado.error)
        assertTrue(estado.pizzas.isEmpty())
    }

    @Test
    fun reintentarTrasUnErrorCargaLaCarta() = runTest {
        val servidor = ServidorSimulado().apply { caido = true }
        val modelo = crearViewModel(servidor)
        modelo.cargarSiHaceFalta()
        assertTrue(modelo.estado.error != null)

        servidor.caido = false
        modelo.cargarSiHaceFalta()

        assertNull(modelo.estado.error)
        assertTrue(modelo.estado.cargada)
        assertEquals(4, modelo.estado.pizzas.size)
    }

    @Test
    fun noRecargaSiYaEstabaCargada() = runTest {
        val servidor = ServidorSimulado()
        val modelo = crearViewModel(servidor)

        modelo.cargarSiHaceFalta()
        modelo.cargarSiHaceFalta()

        assertEquals(1, servidor.peticionesPizzas)
        assertEquals(1, servidor.peticionesMetadatos)
    }

    // --- Filtrado por categoría ----------------------------------------------

    @Test
    fun todasMuestraTodasLasPizzas() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        assertEquals(listOf(1, 2, 3, 4), idsFiltrados(modelo, null))
    }

    @Test
    fun filtraPorCategoria() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        assertEquals(listOf(1, 2), idsFiltrados(modelo, 1))
        assertEquals(listOf(4), idsFiltrados(modelo, 2))
    }

    @Test
    fun unaCategoriaSinPizzasDejaLaListaVacia() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        assertTrue(idsFiltrados(modelo, 3).isEmpty())
        // La carta no está vacía: solo lo está la categoría elegida.
        assertEquals(4, modelo.estado.pizzas.size)
    }

    @Test
    fun laPizzaSinCategoriaSoloApareceEnTodas() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        for (categoria in modelo.estado.categorias) {
            assertFalse(3 in idsFiltrados(modelo, categoria.id), "Categoría ${categoria.id}")
        }
        assertTrue(3 in idsFiltrados(modelo, null))
    }

    @Test
    fun volverATodasQuitaElFiltro() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        modelo.seleccionarCategoria(2)
        assertEquals(1, modelo.estado.pizzasFiltradas.size)
        modelo.seleccionarCategoria(null)

        assertEquals(4, modelo.estado.pizzasFiltradas.size)
    }

    @Test
    fun recargarConservaLaCategoriaElegida() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()
        modelo.seleccionarCategoria(2)

        modelo.cargar()

        assertEquals(2, modelo.estado.categoriaSeleccionadaId)
    }

    @Test
    fun lasPizzasAgotadasSiguenEnLaLista() = runTest {
        val modelo = crearViewModel(ServidorSimulado())
        modelo.cargar()

        val agotadas = modelo.estado.pizzasFiltradas.filter { !it.disponible }

        assertEquals(listOf(3), agotadas.map { it.id })
    }
}
