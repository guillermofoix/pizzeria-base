package es.fpmola.pizzeria.ui.pedidos

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.pagos.AbridorUrl
import es.fpmola.pizzeria.pagos.ErrorPago
import es.fpmola.pizzeria.pagos.RepositorioPagos
import es.fpmola.pizzeria.pedidos.EstadoPedido
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.prueba.AbridorFalso
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_SESION_PAGO
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.jsonPedido
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PedidoConfirmadoViewModelTest {

    private fun crearViewModel(
        motor: MockEngine,
        pedidoInicial: Pedido? = null,
        abridor: AbridorUrl = AbridorFalso(),
    ): PedidoConfirmadoViewModel {
        val cliente = crearClientePrueba(AjustesEnMemoria(), motor)
        return PedidoConfirmadoViewModel(
            repositorio = RepositorioPedidos(cliente),
            repositorioPagos = RepositorioPagos(cliente),
            abridor = abridor,
            pedidoId = 104,
            pedidoInicial = pedidoInicial,
        )
    }

    /** Motor que responde con un estado distinto en cada consulta, por orden. */
    private class EstadosEnSecuencia(private val estados: List<String>) {
        var consultas = 0
            private set

        val motor = MockEngine { solicitud ->
            // Las consultas se hacen una detrás de otra, así que el contador no se pisa.
            val estado = estados[minOf(consultas, estados.lastIndex)]
            consultas++
            check(solicitud.url.encodedPath == "/api/pedidos/104")
            responderJson(jsonPedido(estado = estado))
        }
    }

    @Test
    fun elSondeoSeDetieneCuandoElEstadoEsFinal() = runTest {
        val servidor = EstadosEnSecuencia(listOf("pendiente", "en_preparacion", "entregado", "pendiente"))
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        // Tres consultas: se para en "entregado" y no pide la cuarta.
        assertEquals(3, servidor.consultas)
        assertEquals("entregado", modelo.estado.pedido?.estado)
        assertTrue(modelo.estado.pedido!!.esFinal)
        assertFalse(modelo.estado.actualizando)
    }

    @Test
    fun cadaEstadoFinalDetieneElSondeo() = runTest {
        for (estadoFinal in listOf("servido", "entregado", "cancelado")) {
            val servidor = EstadosEnSecuencia(listOf("pendiente", estadoFinal))
            val modelo = crearViewModel(servidor.motor)

            modelo.sondear(intervaloMs = 1_000)

            assertEquals(2, servidor.consultas, "Estado final: $estadoFinal")
        }
    }

    @Test
    fun siElPrimerEstadoYaEsFinalSoloHaceUnaConsulta() = runTest {
        val servidor = EstadosEnSecuencia(listOf("cancelado"))
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(1, servidor.consultas)
    }

    @Test
    fun mientrasElEstadoNoEsFinalElSondeoSigue() = runTest {
        // Cinco estados no finales y, al final, uno final.
        val servidor = EstadosEnSecuencia(
            listOf("pendiente", "en_preparacion", "listo", "en_reparto", "pendiente", "entregado"),
        )
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(6, servidor.consultas)
    }

    @Test
    fun unFalloDeRedNoDetieneElSondeo() = runTest {
        var consultas = 0
        val motor = MockEngine {
            consultas++
            if (consultas == 1) throw RuntimeException("Red no disponible (simulada)")
            responderJson(jsonPedido(estado = "entregado"))
        }
        val modelo = crearViewModel(motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(2, consultas)
        assertEquals("entregado", modelo.estado.pedido?.estado)
        assertNull(modelo.estado.error)
    }

    @Test
    fun siLaConsultaFallaSeConservaElUltimoPedidoYSeGuardaElError() = runTest {
        val inicial = Pedido(
            id = 104,
            tipoPedido = "domicilio",
            estado = "pendiente",
            total = Importe(3250),
        )
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }
        val modelo = crearViewModel(motor, pedidoInicial = inicial)

        modelo.actualizar()

        assertEquals(inicial, modelo.estado.pedido)
        assertEquals(ErrorRed.SinConexion().mensajeUsuario, modelo.estado.error)
        assertFalse(modelo.estado.actualizando)
    }

    @Test
    fun actualizarManualmenteTraeElEstadoNuevo() = runTest {
        val servidor = EstadosEnSecuencia(listOf("en_preparacion"))
        val modelo = crearViewModel(servidor.motor)

        modelo.actualizar()

        assertEquals(EstadoPedido.EnPreparacion, modelo.estado.pedido?.estadoConocido)
        assertEquals(1, servidor.consultas)
        assertNotNull(modelo.estado.pedido)
        assertNull(modelo.estado.error)
    }

    // --- Pago con tarjeta: sondeo ----------------------------------------------

    /**
     * Servidor que responde a cada consulta con el par (estado, estado_pago)
     * que toca, por orden, y apunta qué rutas se piden (para comprobar que el
     * sondeo nunca llama a confirmar-sesion).
     */
    private class PedidoConPagoEnSecuencia(
        private val pasos: List<Pair<String, String>>,
        private val metodoPago: String = "stripe",
    ) {
        val peticiones = mutableListOf<String>()
        val consultas: Int
            get() = peticiones.size

        val motor = MockEngine { solicitud ->
            val (estado, estadoPago) = pasos[minOf(peticiones.size, pasos.lastIndex)]
            peticiones += "${solicitud.method.value} ${solicitud.url.encodedPath}"
            responderJson(jsonPedido(estado = estado, estadoPago = estadoPago, metodoPago = metodoPago))
        }
    }

    @Test
    fun conTarjetaPendienteElSondeoNoSeDetieneEnUnEstadoFinal() = runTest {
        val servidor = PedidoConPagoEnSecuencia(
            listOf(
                "entregado" to "pendiente",
                "entregado" to "pendiente",
                "entregado" to "pagado",
                "entregado" to "pendiente",
            ),
        )
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        // Sigue con el pedido ya entregado hasta que el pago consta; no pide la cuarta.
        assertEquals(3, servidor.consultas)
        assertTrue(modelo.estado.pedido!!.pagado)
        assertFalse(modelo.estado.esperaPagoAgotada)
        // Solo lee el pedido: nunca llama a confirmar-sesion.
        assertEquals(List(3) { "GET /api/pedidos/104" }, servidor.peticiones)
    }

    @Test
    fun conTarjetaPagadaElSondeoSigueHastaQueElEstadoSeaFinal() = runTest {
        val servidor = PedidoConPagoEnSecuencia(
            listOf(
                "pendiente" to "pendiente",
                "en_preparacion" to "pagado",
                "listo" to "pagado",
                "entregado" to "pagado",
                "pendiente" to "pagado",
            ),
        )
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(4, servidor.consultas)
    }

    @Test
    fun conTarjetaPagadaYEstadoFinalSoloHaceUnaConsulta() = runTest {
        val servidor = PedidoConPagoEnSecuencia(listOf("servido" to "pagado"))
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(1, servidor.consultas)
    }

    @Test
    fun conTarjetaSinPagarElSondeoSeDetieneALos15Minutos() = runTest {
        val servidor = PedidoConPagoEnSecuencia(listOf("pendiente" to "pendiente"))
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 60_000, limitePagoMs = 15 * 60_000)

        // Una consulta por minuto, del minuto 0 al 15 (inclusive).
        assertEquals(16, servidor.consultas)
        assertTrue(modelo.estado.esperaPagoAgotada)
        assertTrue(modelo.estado.pedido!!.pagoConTarjetaPendiente)
    }

    @Test
    fun siNoEsConTarjetaElLimiteDeEsperaNoSeAplica() = runTest {
        val pasos = List(20) { "pendiente" to "pendiente" } + ("entregado" to "pendiente")
        val servidor = PedidoConPagoEnSecuencia(pasos, metodoPago = "efectivo_entrega")
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 60_000, limitePagoMs = 15 * 60_000)

        assertEquals(21, servidor.consultas)
        assertFalse(modelo.estado.esperaPagoAgotada)
    }

    @Test
    fun unPedidoCanceladoNoEsperaPagoAunqueSigaPendiente() = runTest {
        val servidor = PedidoConPagoEnSecuencia(listOf("cancelado" to "pendiente"))
        val modelo = crearViewModel(servidor.motor)

        modelo.sondear(intervaloMs = 1_000)

        assertEquals(1, servidor.consultas)
        assertFalse(modelo.estado.pedido!!.pagoConTarjetaPendiente)
    }

    // --- Pago con tarjeta: botones ---------------------------------------------

    private fun pedidoConTarjeta() = Pedido(
        id = 104,
        tipoPedido = "domicilio",
        estado = "pendiente",
        total = Importe(3250),
        metodoPago = "stripe",
        estadoPago = "pendiente",
    )

    @Test
    fun comprobarPagoEnviaSoloElPedidoIdYRefrescaElPedido() = runTest {
        val peticiones = mutableListOf<String>()
        var cuerpoConfirmar: String? = null
        val motor = MockEngine { solicitud ->
            peticiones += "${solicitud.method.value} ${solicitud.url.encodedPath}"
            if (solicitud.url.encodedPath == "/api/pagos/confirmar-sesion") {
                cuerpoConfirmar = (solicitud.body as TextContent).text
                responderJson("""{"success":true,"estado_pago":"pagado","pedido_id":104}""")
            } else {
                responderJson(jsonPedido(estado = "pendiente", estadoPago = "pagado", metodoPago = "stripe"))
            }
        }
        val modelo = crearViewModel(motor, pedidoInicial = pedidoConTarjeta())

        modelo.comprobarPago()

        assertEquals("""{"pedido_id":104}""", cuerpoConfirmar)
        assertEquals(listOf("POST /api/pagos/confirmar-sesion", "GET /api/pedidos/104"), peticiones)
        assertTrue(modelo.estado.pedido!!.pagado)
        assertFalse(modelo.estado.procesandoPago)
        assertNull(modelo.estado.errorPago)
    }

    @Test
    fun comprobarPagoSinPagarAvisaYNoCambiaElPedido() = runTest {
        val peticiones = mutableListOf<String>()
        val motor = MockEngine { solicitud ->
            peticiones += solicitud.url.encodedPath
            responderJson("""{"success":true,"estado_pago":"unpaid","message":"Estado actual en Stripe: unpaid"}""")
        }
        val modelo = crearViewModel(motor, pedidoInicial = pedidoConTarjeta())

        modelo.comprobarPago()

        assertEquals(listOf("/api/pagos/confirmar-sesion"), peticiones)
        assertFalse(modelo.estado.pedido!!.pagado)
        assertTrue(modelo.estado.infoPago!!.contains("Todavía no consta el pago"), modelo.estado.infoPago)
        assertFalse(modelo.estado.procesandoPago)
    }

    @Test
    fun comprobarPagoMuestraElErrorSiFalla() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }
        val modelo = crearViewModel(motor, pedidoInicial = pedidoConTarjeta())

        modelo.comprobarPago()

        assertEquals(ErrorPago.SinConexion.mensajeUsuario, modelo.estado.errorPago)
        assertFalse(modelo.estado.procesandoPago)
    }

    @Test
    fun pagarConTarjetaPideUnaSesionNuevaYAbreLaUrlSinConfirmar() = runTest {
        val peticiones = mutableListOf<String>()
        val motor = MockEngine { solicitud ->
            peticiones += solicitud.url.encodedPath
            responderJson(JSON_SESION_PAGO)
        }
        val abridor = AbridorFalso()
        val modelo = crearViewModel(motor, pedidoInicial = pedidoConTarjeta(), abridor = abridor)

        modelo.pagarConTarjeta()

        assertEquals(listOf("/api/pagos/crear-sesion"), peticiones)
        assertEquals(1, abridor.abiertas.size)
        assertNull(modelo.estado.errorPago)
        assertNotNull(modelo.estado.infoPago)
        assertFalse(modelo.estado.procesandoPago)
    }

    @Test
    fun pagarConTarjetaMuestraElErrorYNoAbreNadaSiFalla() = runTest {
        val motor = MockEngine {
            responderJson("""{"success":false}""", HttpStatusCode.ServiceUnavailable)
        }
        val abridor = AbridorFalso()
        val modelo = crearViewModel(motor, pedidoInicial = pedidoConTarjeta(), abridor = abridor)

        modelo.pagarConTarjeta()

        assertEquals(ErrorPago.PagoNoDisponible.mensajeUsuario, modelo.estado.errorPago)
        assertTrue(abridor.abiertas.isEmpty())
        assertFalse(modelo.estado.procesandoPago)
    }
}
