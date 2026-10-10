package es.fpmola.pizzeria.pedidos

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_PEDIDO_CREADO
import es.fpmola.pizzeria.prueba.URL_PRUEBA
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.jsonPedido
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class RepositorioPedidosTest {

    private val peticion = PeticionPedido(
        tipoPedido = "domicilio",
        clienteNombre = "Ana López",
        clienteTelefono = "600123456",
        clienteDireccion = "Calle Mayor 1, 2º A",
        metodoPago = "efectivo_entrega",
        observaciones = "Llamar al timbre",
        lineas = listOf(
            LineaPeticion(pizzaId = 1, cantidad = 2),
            LineaPeticion(pizzaId = 3, cantidad = 1, notas = "Sin gorgonzola"),
        ),
    )

    private fun repositorio(motor: MockEngine, ajustes: AjustesEnMemoria = AjustesEnMemoria()) =
        RepositorioPedidos(crearClientePrueba(ajustes, motor))

    // --- Crear pedido --------------------------------------------------------

    @Test
    fun creaElPedidoConUnPostAPedidos() = runTest {
        var metodo: HttpMethod? = null
        var url: String? = null
        var tipoContenido: String? = null
        var cuerpo: String? = null
        val motor = MockEngine { solicitud ->
            metodo = solicitud.method
            url = solicitud.url.toString()
            tipoContenido = solicitud.body.contentType?.toString()
            cuerpo = (solicitud.body as TextContent).text
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }

        val resultado = repositorio(motor).crearPedido(peticion)

        val creado = assertIs<ResultadoEnvioPedido.Creado>(resultado)
        assertEquals(104, creado.pedido.id)
        assertEquals("pendiente", creado.pedido.estado)
        assertEquals(Importe(3250), creado.pedido.total)
        assertEquals(TipoPedido.Domicilio, creado.pedido.tipo)
        assertEquals(2, creado.pedido.lineas.size)
        assertEquals(Importe(1900), creado.pedido.lineas.first().subtotalCalculado)

        assertEquals(HttpMethod.Post, metodo)
        assertEquals("$URL_PRUEBA/api/pedidos", url)
        assertTrue(tipoContenido!!.startsWith("application/json"), tipoContenido)
        assertEquals(
            Json.parseToJsonElement(
                """
                {"tipo_pedido":"domicilio","cliente_nombre":"Ana López","cliente_telefono":"600123456",
                 "cliente_direccion":"Calle Mayor 1, 2º A","metodo_pago":"efectivo_entrega",
                 "observaciones":"Llamar al timbre",
                 "lineas":[{"pizza_id":1,"cantidad":2},{"pizza_id":3,"cantidad":1,"notas":"Sin gorgonzola"}]}
                """.trimIndent(),
            ),
            Json.parseToJsonElement(cuerpo!!),
        )
    }

    @Test
    fun errorDeValidacionDelServidorSeDevuelveConSuMensaje() = runTest {
        val motor = MockEngine {
            responderJson(
                """{"success":false,"message":"La pizza con ID 99 no existe."}""",
                HttpStatusCode.BadRequest,
            )
        }

        val resultado = repositorio(motor).crearPedido(peticion)

        assertEquals(
            ResultadoEnvioPedido.Rechazado("La pizza con ID 99 no existe.", 400),
            resultado,
        )
    }

    @Test
    fun errorDe4xxSinCuerpoLegibleUsaUnMensajeGenerico() = runTest {
        val motor = MockEngine { responderJson("no es json", HttpStatusCode.BadRequest) }

        val resultado = repositorio(motor).crearPedido(peticion)

        assertEquals(
            ResultadoEnvioPedido.Rechazado("El servidor no ha aceptado el pedido (código 400).", 400),
            resultado,
        )
    }

    @Test
    fun error500DejaElResultadoIncierto() = runTest {
        val motor = MockEngine {
            responderJson("""{"success":false,"message":"Error interno"}""", HttpStatusCode.InternalServerError)
        }

        val resultado = repositorio(motor).crearPedido(peticion)

        val incierto = assertIs<ResultadoEnvioPedido.Incierto>(resultado)
        assertTrue(incierto.mensaje.contains("código 500"), incierto.mensaje)
        assertTrue(incierto.mensaje.contains("comprueba con el local"), incierto.mensaje)
        assertTrue(incierto.mensaje.contains("carrito se mantiene"), incierto.mensaje)
    }

    @Test
    fun sinConexionExplicaQueQuizaElPedidoSiLlego() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }

        val resultado = repositorio(motor).crearPedido(peticion)

        val incierto = assertIs<ResultadoEnvioPedido.Incierto>(resultado)
        assertTrue(incierto.mensaje.contains("Es posible que el pedido sí se haya registrado"), incierto.mensaje)
        assertTrue(incierto.mensaje.contains("comprueba con el local"), incierto.mensaje)
    }

    @Test
    fun noReintentaUnPostAutomaticamente() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            throw RuntimeException("Red no disponible (simulada)")
        }

        repositorio(motor).crearPedido(peticion)

        assertEquals(1, peticiones)
    }

    @Test
    fun respuestaCorrectaQueNoSeEntiendeEsIncierta() = runTest {
        // El servidor dice 201 pero el cuerpo no es un pedido: pudo crearse.
        val motor = MockEngine { responderJson("""{"success":true,"data":"raro"}""", HttpStatusCode.Created) }

        val resultado = repositorio(motor).crearPedido(peticion)

        assertIs<ResultadoEnvioPedido.Incierto>(resultado)
    }

    @Test
    fun sinServidorConfiguradoNoSeEnviaNada() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }

        val resultado = repositorio(motor, AjustesEnMemoria(url = null)).crearPedido(peticion)

        val noEnviado = assertIs<ResultadoEnvioPedido.NoEnviado>(resultado)
        assertEquals(ErrorRed.SinServidorConfigurado().mensajeUsuario, noEnviado.mensaje)
        assertEquals(0, peticiones)
    }

    // --- Consultar pedido ----------------------------------------------------

    @Test
    fun consultaUnPedidoPorSuId() = runTest {
        var url: String? = null
        val motor = MockEngine { solicitud ->
            url = solicitud.url.toString()
            responderJson(jsonPedido(estado = "en_preparacion"))
        }

        val pedido = repositorio(motor).obtenerPedido(104)

        assertEquals("$URL_PRUEBA/api/pedidos/104", url)
        assertEquals("en_preparacion", pedido.estado)
        assertEquals(EstadoPedido.EnPreparacion, pedido.estadoConocido)
        assertEquals("pendiente", pedido.estadoPago)
        assertEquals(Importe(1900), pedido.lineas.first().subtotal)
        assertEquals(Importe(1350), pedido.lineas.last().subtotalCalculado)
    }

    @Test
    fun consultarUnPedidoInexistenteEsErrorDeServidor() = runTest {
        val motor = MockEngine {
            responderJson("""{"success":false,"message":"Pedido #999 no encontrado"}""", HttpStatusCode.NotFound)
        }

        val error = assertFailsWith<ErrorRed.Servidor> { repositorio(motor).obtenerPedido(999) }

        assertEquals(404, error.codigo)
    }

    @Test
    fun consultarSinConexion() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }

        assertFailsWith<ErrorRed.SinConexion> { repositorio(motor).obtenerPedido(104) }
    }

    @Test
    fun unEstadoDesconocidoNoEsFinal() = runTest {
        val motor = MockEngine { responderJson(jsonPedido(estado = "estado_nuevo")) }

        val pedido = repositorio(motor).obtenerPedido(104)

        assertEquals("estado_nuevo", pedido.estado)
        assertEquals(null, pedido.estadoConocido)
        assertEquals(false, pedido.esFinal)
    }
}
