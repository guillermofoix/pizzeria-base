package es.fpmola.pizzeria.salud

import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.URL_PRUEBA
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RepositorioSaludTest {

    private val saludCorrecta = """
        {"status":"UP","service":"pizzeria-backend","timestamp":"2026-10-10T10:15:00.000Z",
         "database":{"connected":true,"db_time":"2026-10-10T10:15:00.012Z"}}
    """.trimIndent()

    private val saludDegradada = """
        {"status":"DEGRADED","service":"pizzeria-backend","timestamp":"2026-10-10T10:15:00.000Z",
         "database":{"connected":false,"error":"Connection terminated due to connection timeout"}}
    """.trimIndent()

    private fun repositorio(motor: MockEngine, ajustes: AjustesEnMemoria = AjustesEnMemoria()) =
        RepositorioSalud(crearClientePrueba(ajustes, motor))

    @Test
    fun respuesta200ConServidorYBaseDeDatosOperativos() = runTest {
        var urlPedida: String? = null
        val motor = MockEngine { peticion ->
            urlPedida = peticion.url.toString()
            responderJson(saludCorrecta)
        }

        val resultado = repositorio(motor).comprobar()

        val correcta = assertIs<ResultadoSalud.Correcta>(resultado)
        assertEquals("UP", correcta.salud.estado)
        assertTrue(correcta.salud.baseDatos.conectada)
        assertEquals("$URL_PRUEBA/api/health", urlPedida)
    }

    @Test
    fun respuesta500EsBaseDeDatosCaida() = runTest {
        val motor = MockEngine { responderJson(saludDegradada, HttpStatusCode.InternalServerError) }

        val resultado = repositorio(motor).comprobar()

        assertEquals(
            ResultadoSalud.Fallida("El servidor responde, pero la base de datos no"),
            resultado,
        )
    }

    @Test
    fun respuesta200ConBaseDeDatosDesconectadaNoEsCorrecta() = runTest {
        val cuerpo = """{"status":"UP","database":{"connected":false}}"""
        val motor = MockEngine { responderJson(cuerpo) }

        val resultado = repositorio(motor).comprobar()

        assertEquals(ResultadoSalud.Fallida(RepositorioSalud.MENSAJE_BD_CAIDA), resultado)
    }

    @Test
    fun respuesta200SinEstadoUpNoEsCorrecta() = runTest {
        val cuerpo = """{"status":"MAINTENANCE","database":{"connected":true}}"""
        val motor = MockEngine { responderJson(cuerpo) }

        assertIs<ResultadoSalud.Fallida>(repositorio(motor).comprobar())
    }

    @Test
    fun respuesta200QueNoEsLaSaludEsRespuestaInvalida() = runTest {
        val motor = MockEngine { responderJson("""{"success":true}""") }

        val resultado = repositorio(motor).comprobar()

        assertEquals(ResultadoSalud.Fallida(ErrorRed.RespuestaInvalida().mensajeUsuario), resultado)
    }

    @Test
    fun otroCodigoDeErrorIndicaElCodigo() = runTest {
        val motor = MockEngine { responderJson("{}", HttpStatusCode.BadGateway) }

        val resultado = repositorio(motor).comprobar()

        assertEquals(ResultadoSalud.Fallida(ErrorRed.Servidor(502).mensajeUsuario), resultado)
    }

    @Test
    fun sinConexion() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }

        val resultado = repositorio(motor).comprobar()

        assertEquals(ResultadoSalud.Fallida(ErrorRed.SinConexion().mensajeUsuario), resultado)
    }

    @Test
    fun pruebaUnaUrlDistintaDeLaGuardada() = runTest {
        var urlPedida: String? = null
        val motor = MockEngine { peticion ->
            urlPedida = peticion.url.toString()
            responderJson(saludCorrecta)
        }

        repositorio(motor, AjustesEnMemoria(url = null)).comprobar("https://otro.servidor")

        assertEquals("https://otro.servidor/api/health", urlPedida)
    }
}
