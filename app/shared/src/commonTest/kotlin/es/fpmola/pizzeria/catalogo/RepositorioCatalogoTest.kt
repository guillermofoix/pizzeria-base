package es.fpmola.pizzeria.catalogo

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_METADATOS
import es.fpmola.pizzeria.prueba.JSON_PIZZAS
import es.fpmola.pizzeria.prueba.URL_PRUEBA
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class RepositorioCatalogoTest {

    private fun repositorio(motor: MockEngine, ajustes: AjustesEnMemoria = AjustesEnMemoria()) =
        RepositorioCatalogo(crearClientePrueba(ajustes, motor))

    @Test
    fun obtienePizzasDeLaRutaDeLaCarta() = runTest {
        var urlPedida: String? = null
        val motor = MockEngine { peticion ->
            urlPedida = peticion.url.toString()
            responderJson(JSON_PIZZAS)
        }

        val pizzas = repositorio(motor).obtenerPizzas()

        assertEquals(4, pizzas.size)
        assertEquals(Importe(950), pizzas.first().precio)
        assertEquals("$URL_PRUEBA/api/pizzas", urlPedida)
    }

    @Test
    fun obtieneLosMetadatosDeSuRuta() = runTest {
        var urlPedida: String? = null
        val motor = MockEngine { peticion ->
            urlPedida = peticion.url.toString()
            responderJson(JSON_METADATOS)
        }

        val metadatos = repositorio(motor).obtenerMetadatos()

        assertEquals(4, metadatos.categorias.size)
        assertEquals("$URL_PRUEBA/api/pizzas/metadata", urlPedida)
    }

    @Test
    fun errorDelServidorConservaElCodigo() = runTest {
        val motor = MockEngine {
            responderJson(
                """{"success":false,"message":"Error interno al consultar la carta"}""",
                HttpStatusCode.InternalServerError,
            )
        }

        val error = assertFailsWith<ErrorRed.Servidor> { repositorio(motor).obtenerPizzas() }

        assertEquals(500, error.codigo)
    }

    @Test
    fun sinConexion() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }

        assertFailsWith<ErrorRed.SinConexion> { repositorio(motor).obtenerPizzas() }
    }

    @Test
    fun sinServidorConfiguradoNoPideNada() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_PIZZAS)
        }

        assertFailsWith<ErrorRed.SinServidorConfigurado> {
            repositorio(motor, AjustesEnMemoria(url = null)).obtenerPizzas()
        }
        assertEquals(0, peticiones)
    }

    @Test
    fun cuerpoQueNoEsLaCartaEsRespuestaInvalida() = runTest {
        val motor = MockEngine { responderJson("""{"success":true,"data":"no es una lista"}""") }

        assertFailsWith<ErrorRed.RespuestaInvalida> { repositorio(motor).obtenerPizzas() }
    }

    @Test
    fun respuestaSinExitoOSinDatosEsRespuestaInvalida() = runTest {
        val sinExito = MockEngine { responderJson("""{"success":false,"data":[]}""") }
        val sinDatos = MockEngine { responderJson("""{"success":true}""") }

        assertFailsWith<ErrorRed.RespuestaInvalida> { repositorio(sinExito).obtenerPizzas() }
        assertFailsWith<ErrorRed.RespuestaInvalida> { repositorio(sinDatos).obtenerPizzas() }
    }
}
