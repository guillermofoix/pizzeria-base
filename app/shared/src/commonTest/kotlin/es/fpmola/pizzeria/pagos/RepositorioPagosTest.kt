package es.fpmola.pizzeria.pagos

import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_SESION_PAGO
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RepositorioPagosTest {

    private fun crearRepositorio(motor: MockEngine) =
        RepositorioPagos(crearClientePrueba(AjustesEnMemoria(), motor))

    private fun errorDe(resultado: ResultadoSesionPago): ErrorPago =
        assertIs<ResultadoSesionPago.Fallida>(resultado).error

    // --- crear-sesion: respuesta correcta ------------------------------------

    @Test
    fun laRespuestaSinEnvoltorioSeLeeConUrlYSessionId() = runTest {
        val repositorio = crearRepositorio(MockEngine { responderJson(JSON_SESION_PAGO) })

        val resultado = repositorio.crearSesion(104)

        val creada = assertIs<ResultadoSesionPago.Creada>(resultado)
        assertTrue(creada.url.startsWith("https://checkout.stripe.com/c/pay/cs_test_a1B2c3"), creada.url)
        assertEquals("cs_test_a1B2c3", creada.idSesion)
    }

    @Test
    fun crearSesionHaceUnPostConSoloElPedidoId() = runTest {
        var metodo: HttpMethod? = null
        var ruta: String? = null
        var cuerpo: String? = null
        val repositorio = crearRepositorio(
            MockEngine { solicitud ->
                metodo = solicitud.method
                ruta = solicitud.url.encodedPath
                cuerpo = (solicitud.body as TextContent).text
                responderJson(JSON_SESION_PAGO)
            },
        )

        repositorio.crearSesion(104)

        assertEquals(HttpMethod.Post, metodo)
        assertEquals("/api/pagos/crear-sesion", ruta)
        assertEquals("""{"pedido_id":104}""", cuerpo)
    }

    @Test
    fun siFaltaLaUrlONoEsCorrectaLaRespuestaSeRechaza() = runTest {
        val cuerposInvalidos = listOf(
            """{"success":true,"sessionId":"cs_test_1"}""",
            """{"success":true,"url":"","sessionId":"cs_test_1"}""",
            """{"success":false,"url":"https://checkout.stripe.com/x"}""",
            """{"success":true,"data":{"url":"https://checkout.stripe.com/x"}}""",
            "esto no es JSON",
        )
        for (cuerpoInvalido in cuerposInvalidos) {
            val repositorio = crearRepositorio(MockEngine { responderJson(cuerpoInvalido) })

            val resultado = repositorio.crearSesion(104)

            assertIs<ErrorPago.Otro>(errorDe(resultado), cuerpoInvalido)
        }
    }

    // --- crear-sesion: errores -----------------------------------------------

    @Test
    fun unPedidoYaPagadoSeTipaComoPedidoYaPagado() = runTest {
        val repositorio = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":false,"message":"El pedido #104 ya fue pagado anteriormente"}""",
                    HttpStatusCode.BadRequest,
                )
            },
        )

        assertEquals(ErrorPago.PedidoYaPagado, errorDe(repositorio.crearSesion(104)))
    }

    @Test
    fun otroErrorDe400MuestraElMensajeDelServidor() = runTest {
        val repositorio = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":false,"message":"El pedido #104 no tiene pizzas asociadas"}""",
                    HttpStatusCode.BadRequest,
                )
            },
        )

        val error = assertIs<ErrorPago.Otro>(errorDe(repositorio.crearSesion(104)))
        assertEquals("El pedido #104 no tiene pizzas asociadas", error.mensajeUsuario)
    }

    @Test
    fun unPedidoInexistenteSeTipaComoOtro() = runTest {
        val repositorio = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":false,"message":"El pedido #999 no existe"}""",
                    HttpStatusCode.NotFound,
                )
            },
        )

        val error = assertIs<ErrorPago.Otro>(errorDe(repositorio.crearSesion(999)))
        assertEquals("El pedido #999 no existe", error.mensajeUsuario)
    }

    @Test
    fun stripeSinConfigurarSeTipaComoPagoNoDisponible() = runTest {
        val repositorio = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":false,"message":"La pasarela de pagos Stripe no está configurada"}""",
                    HttpStatusCode.ServiceUnavailable,
                )
            },
        )

        assertEquals(ErrorPago.PagoNoDisponible, errorDe(repositorio.crearSesion(104)))
    }

    @Test
    fun unErrorDelServidorSinCuerpoLegibleDaUnMensajeGenerico() = runTest {
        val repositorio = crearRepositorio(
            MockEngine { responderJson("<html>Bad gateway</html>", HttpStatusCode.BadGateway) },
        )

        val error = assertIs<ErrorPago.Otro>(errorDe(repositorio.crearSesion(104)))
        assertTrue(error.mensajeUsuario.contains("502"), error.mensajeUsuario)
    }

    @Test
    fun sinConexionSeTipaComoSinConexion() = runTest {
        val repositorio = crearRepositorio(MockEngine { throw RuntimeException("Red no disponible (simulada)") })

        assertEquals(ErrorPago.SinConexion, errorDe(repositorio.crearSesion(104)))
    }

    @Test
    fun sinServidorConfiguradoNoSeEnviaNada() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_SESION_PAGO)
        }
        val repositorio = RepositorioPagos(crearClientePrueba(AjustesEnMemoria(url = null), motor))

        assertIs<ErrorPago.Otro>(errorDe(repositorio.crearSesion(104)))
        assertEquals(0, peticiones)
    }

    // --- confirmar-sesion ----------------------------------------------------

    @Test
    fun confirmarSesionEnviaSoloElPedidoIdYNuncaSessionId() = runTest {
        var metodo: HttpMethod? = null
        var ruta: String? = null
        var cuerpo: String? = null
        val repositorio = crearRepositorio(
            MockEngine { solicitud ->
                metodo = solicitud.method
                ruta = solicitud.url.encodedPath
                cuerpo = (solicitud.body as TextContent).text
                responderJson("""{"success":true,"estado_pago":"pagado","pedido_id":104}""")
            },
        )

        repositorio.confirmarPago(104)

        assertEquals(HttpMethod.Post, metodo)
        assertEquals("/api/pagos/confirmar-sesion", ruta)
        assertEquals("""{"pedido_id":104}""", cuerpo)
        assertFalse(cuerpo!!.contains("session"), cuerpo)
    }

    @Test
    fun confirmarPagoDevuelvePagadoSiElServidorLoConfirma() = runTest {
        val repositorio = crearRepositorio(
            MockEngine { responderJson("""{"success":true,"estado_pago":"pagado","pedido_id":104}""") },
        )

        assertEquals(ResultadoConfirmacionPago.Pagado, repositorio.confirmarPago(104))
    }

    @Test
    fun confirmarPagoDevuelveTodaviaSinPagarSiStripeNoHaCobrado() = runTest {
        val repositorio = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":true,"estado_pago":"unpaid","message":"Estado actual en Stripe: unpaid"}""",
                )
            },
        )

        val resultado = repositorio.confirmarPago(104)

        assertEquals(ResultadoConfirmacionPago.TodaviaSinPagar("unpaid"), resultado)
    }

    @Test
    fun confirmarPagoTipaLosErrores() = runTest {
        val sinSesion = crearRepositorio(
            MockEngine {
                responderJson(
                    """{"success":false,"message":"No se encontró sesión de Stripe asociada"}""",
                    HttpStatusCode.BadRequest,
                )
            },
        ).confirmarPago(104)
        val sinStripe = crearRepositorio(
            MockEngine { responderJson("""{"success":false}""", HttpStatusCode.ServiceUnavailable) },
        ).confirmarPago(104)
        val sinRed = crearRepositorio(
            MockEngine { throw RuntimeException("Red no disponible (simulada)") },
        ).confirmarPago(104)
        val ilegible = crearRepositorio(
            MockEngine { responderJson("""{"success":true}""") },
        ).confirmarPago(104)

        assertEquals(
            ErrorPago.Otro("No se encontró sesión de Stripe asociada"),
            assertIs<ResultadoConfirmacionPago.Fallida>(sinSesion).error,
        )
        assertEquals(ErrorPago.PagoNoDisponible, assertIs<ResultadoConfirmacionPago.Fallida>(sinStripe).error)
        assertEquals(ErrorPago.SinConexion, assertIs<ResultadoConfirmacionPago.Fallida>(sinRed).error)
        assertIs<ErrorPago.Otro>(assertIs<ResultadoConfirmacionPago.Fallida>(ilegible).error)
    }
}
