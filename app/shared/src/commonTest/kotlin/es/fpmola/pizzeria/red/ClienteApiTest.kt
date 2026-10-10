package es.fpmola.pizzeria.red

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.modelo.RespuestaApi
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.URL_PRUEBA
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class ClienteApiTest {

    @Serializable
    private data class PizzaPrueba(val id: Int, val nombre: String, val precio: Importe)

    private val listaPizzas = """
        {"success":true,"count":1,"data":[{"id":1,"nombre":"Margherita Clásica","precio":"9.50"}]}
    """.trimIndent()

    @Test
    fun exitoLeeElEnvoltorioYUsaLaUrlGuardada() = runTest {
        var urlPedida: String? = null
        val motor = MockEngine { peticion ->
            urlPedida = peticion.url.toString()
            responderJson(listaPizzas)
        }
        val cliente = crearClientePrueba(motor = motor)

        val respuesta = cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")

        assertTrue(respuesta.success)
        assertEquals(Importe(950), respuesta.data?.single()?.precio)
        assertEquals("$URL_PRUEBA/api/pizzas", urlPedida)
    }

    @Test
    fun laUrlBaseSeLeeEnCadaPeticion() = runTest {
        val urlsPedidas = mutableListOf<String>()
        val motor = MockEngine { peticion ->
            urlsPedidas += peticion.url.toString()
            responderJson(listaPizzas)
        }
        val ajustes = AjustesEnMemoria()
        val cliente = crearClientePrueba(ajustes, motor)

        cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")
        ajustes.guardarUrlBase("https://nuevo.servidor")
        cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")

        assertEquals(
            listOf("$URL_PRUEBA/api/pizzas", "https://nuevo.servidor/api/pizzas"),
            urlsPedidas,
        )
    }

    @Test
    fun error500EsErrorDeServidorConCodigo() = runTest {
        val motor = MockEngine {
            responderJson(
                """{"success":false,"message":"Error interno"}""",
                HttpStatusCode.InternalServerError,
            )
        }
        val cliente = crearClientePrueba(motor = motor)

        val error = assertFailsWith<ErrorRed.Servidor> {
            cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")
        }

        assertEquals(500, error.codigo)
        assertEquals("El servidor ha respondido con un error (código 500).", error.mensajeUsuario)
    }

    @Test
    fun sinConexionEsErrorSinConexion() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }
        val cliente = crearClientePrueba(motor = motor)

        val error = assertFailsWith<ErrorRed.SinConexion> {
            cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")
        }

        assertTrue(error.mensajeUsuario.startsWith("No se ha podido conectar"))
    }

    @Test
    fun cuerpoQueNoEncajaEsRespuestaInvalida() = runTest {
        val motor = MockEngine { responderJson("""{"success":true,"data":"no es una lista"}""") }
        val cliente = crearClientePrueba(motor = motor)

        assertFailsWith<ErrorRed.RespuestaInvalida> {
            cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")
        }
    }

    @Test
    fun sinUrlGuardadaNoSeHacePeticion() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(listaPizzas)
        }
        val cliente = crearClientePrueba(AjustesEnMemoria(url = null), motor)

        assertFailsWith<ErrorRed.SinServidorConfigurado> {
            cliente.obtener<RespuestaApi<List<PizzaPrueba>>>("/api/pizzas")
        }
        assertEquals(0, peticiones)
    }
}
