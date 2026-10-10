package es.fpmola.pizzeria.ui.pedidos

import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.pagos.AVISO_PAGO_NO_INICIADO
import es.fpmola.pizzeria.pagos.AbridorUrl
import es.fpmola.pizzeria.pagos.MENSAJE_URL_NO_SEGURA
import es.fpmola.pizzeria.pagos.RepositorioPagos
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.prueba.AbridorFalso
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_PEDIDO_CREADO
import es.fpmola.pizzeria.prueba.JSON_PEDIDO_CREADO_TARJETA
import es.fpmola.pizzeria.prueba.JSON_SESION_PAGO
import es.fpmola.pizzeria.prueba.carritoConDosPizzas
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class DatosPedidoViewModelTest {

    private fun crearViewModel(
        motor: MockEngine,
        carrito: Carrito = carritoConDosPizzas(),
        abridor: AbridorUrl = AbridorFalso(),
    ): DatosPedidoViewModel {
        val cliente = crearClientePrueba(AjustesEnMemoria(), motor)
        return DatosPedidoViewModel(
            repositorio = RepositorioPedidos(cliente),
            carrito = carrito,
            repositorioPagos = RepositorioPagos(cliente),
            abridor = abridor,
        )
    }

    /** Rellena un pedido a domicilio válido. */
    private fun DatosPedidoViewModel.rellenarDomicilio() {
        cambiarTipo(TipoPedido.Domicilio)
        cambiarNombre("Ana López")
        cambiarTelefono("600 123 456")
        cambiarDireccion("Calle Mayor 1, 2º A")
    }

    // --- Validación antes de enviar ------------------------------------------

    @Test
    fun conDatosNoValidosNoSeEnviaNada() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val modelo = crearViewModel(motor)

        modelo.enviarPedido() // tipo por defecto (domicilio) y sin datos

        assertEquals(0, peticiones)
        assertTrue(modelo.estado.errores.hayErrores)
        assertEquals("El nombre es obligatorio.", modelo.estado.errores.nombre)
        assertFalse(modelo.estado.enviando)
    }

    @Test
    fun conElCarritoVacioNoSeEnviaNada() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val modelo = crearViewModel(motor, carrito = Carrito())
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        assertEquals(0, peticiones)
        assertEquals("El carrito está vacío.", modelo.estado.mensajeError)
    }

    @Test
    fun cambiarDeTipoReiniciaLosErrores() {
        val modelo = crearViewModel(MockEngine { responderJson("{}") })
        modelo.validar()
        assertTrue(modelo.estado.errores.hayErrores)

        modelo.cambiarTipo(TipoPedido.Mesa)

        assertFalse(modelo.estado.errores.hayErrores)
    }

    @Test
    fun losErroresSeRecalculanAlEscribir() {
        val modelo = crearViewModel(MockEngine { responderJson("{}") })
        modelo.cambiarTipo(TipoPedido.Recoger)
        modelo.validar()
        assertNotNull(modelo.estado.errores.nombre)

        modelo.cambiarNombre("Pedro")

        assertNull(modelo.estado.errores.nombre)
        assertNotNull(modelo.estado.errores.telefono)
    }

    // --- Envío correcto ------------------------------------------------------

    @Test
    fun siSeCreaElPedidoSeVaciaElCarritoYSeGuardaElPedido() = runTest {
        var cuerpo: String? = null
        val motor = MockEngine { solicitud ->
            cuerpo = (solicitud.body as TextContent).text
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(motor, carrito)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertFalse(modelo.estado.enviando)
        assertNull(modelo.estado.mensajeError)
        assertTrue(carrito.estaVacio)
        // Los ids enviados son los del carrito (que vienen de la carta).
        assertTrue(cuerpo!!.contains("\"pizza_id\":1"), cuerpo)
        assertTrue(cuerpo!!.contains("\"pizza_id\":3"), cuerpo)
    }

    @Test
    fun unPedidoYaCreadoNoSeEnviaDeNuevo() = runTest {
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val modelo = crearViewModel(motor)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()
        modelo.enviarPedido()

        assertEquals(1, peticiones)
    }

    @Test
    fun mientrasSeEnviaUnSegundoEnvioNoHaceNada() = runTest {
        val peticionEnCurso = CompletableDeferred<Unit>()
        val respuestaLiberada = CompletableDeferred<Unit>()
        var peticiones = 0
        val motor = MockEngine {
            peticiones++
            peticionEnCurso.complete(Unit)
            respuestaLiberada.await()
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val modelo = crearViewModel(motor)
        modelo.rellenarDomicilio()

        val primerEnvio = launch { modelo.enviarPedido() }
        peticionEnCurso.await()
        assertTrue(modelo.estado.enviando)

        modelo.enviarPedido() // segundo toque: se ignora
        respuestaLiberada.complete(Unit)
        primerEnvio.join()

        assertEquals(1, peticiones)
        assertEquals(104, modelo.estado.pedidoCreado?.id)
    }

    // --- Fallos --------------------------------------------------------------

    @Test
    fun siElServidorRechazaElPedidoSeMantieneElCarritoYSePideRefrescarLaCarta() = runTest {
        val motor = MockEngine {
            responderJson(
                """{"success":false,"message":"La pizza con ID 3 no existe."}""",
                HttpStatusCode.BadRequest,
            )
        }
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(motor, carrito)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        val estado = modelo.estado
        assertFalse(carrito.estaVacio)
        assertNull(estado.pedidoCreado)
        assertFalse(estado.enviando)
        assertTrue(estado.cartaPorRefrescar)
        assertTrue(estado.mensajeError!!.startsWith("La pizza con ID 3 no existe."), estado.mensajeError)
        assertTrue(estado.mensajeError!!.contains("actualizado la carta"), estado.mensajeError)

        modelo.cartaRefrescada()
        assertFalse(modelo.estado.cartaPorRefrescar)
    }

    @Test
    fun siFallaLaRedSeMantieneElCarritoYSeAvisaDeQueQuizaLlego() = runTest {
        val motor = MockEngine { throw RuntimeException("Red no disponible (simulada)") }
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(motor, carrito)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        val estado = modelo.estado
        assertFalse(carrito.estaVacio)
        assertNull(estado.pedidoCreado)
        assertFalse(estado.enviando)
        assertTrue(estado.envioIncierto)
        assertFalse(estado.cartaPorRefrescar)
        assertTrue(estado.mensajeError!!.contains("comprueba con el local"), estado.mensajeError)
    }

    @Test
    fun siFallaElServidorSeMantieneElCarrito() = runTest {
        val motor = MockEngine {
            responderJson("""{"success":false}""", HttpStatusCode.InternalServerError)
        }
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(motor, carrito)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        assertFalse(carrito.estaVacio)
        assertTrue(modelo.estado.envioIncierto)
        assertNull(modelo.estado.pedidoCreado)
    }

    @Test
    fun trasUnFalloElUsuarioPuedeVolverAEnviarPeroNuncaSolo() = runTest {
        var peticiones = 0
        var servidorCaido = true
        val motor = MockEngine {
            peticiones++
            if (servidorCaido) {
                throw RuntimeException("Red no disponible (simulada)")
            }
            responderJson(JSON_PEDIDO_CREADO, HttpStatusCode.Created)
        }
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(motor, carrito)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()
        assertEquals(1, peticiones) // sin reintento automático

        servidorCaido = false
        modelo.enviarPedido() // el usuario lo pide de nuevo
        assertEquals(2, peticiones)
        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertTrue(carrito.estaVacio)
    }

    // --- Pago con tarjeta ----------------------------------------------------

    /**
     * Servidor simulado para el flujo con tarjeta: cuenta los pedidos creados y
     * las sesiones pedidas, y responde a crear-sesion con [sesion].
     */
    private class ServidorConTarjeta(sesion: MockRequestHandleScope.() -> HttpResponseData) {
        var pedidosCreados = 0
            private set
        var sesionesPedidas = 0
            private set
        var cuerpoPedido: String? = null
            private set
        var cuerpoSesion: String? = null
            private set

        val motor = MockEngine { solicitud ->
            val cuerpo = (solicitud.body as TextContent).text
            when (solicitud.url.encodedPath) {
                "/api/pedidos" -> {
                    pedidosCreados++
                    cuerpoPedido = cuerpo
                    responderJson(JSON_PEDIDO_CREADO_TARJETA, HttpStatusCode.Created)
                }
                "/api/pagos/crear-sesion" -> {
                    sesionesPedidas++
                    cuerpoSesion = cuerpo
                    sesion(this)
                }
                else -> error("Ruta inesperada: ${solicitud.url.encodedPath}")
            }
        }
    }

    @Test
    fun conTarjetaSeCreaElPedidoSeCreaLaSesionYSeAbreLaUrl() = runTest {
        val servidor = ServidorConTarjeta { responderJson(JSON_SESION_PAGO) }
        val abridor = AbridorFalso()
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(servidor.motor, carrito, abridor)
        modelo.rellenarDomicilio()
        modelo.cambiarPagoConTarjeta(true)

        modelo.enviarPedido()

        assertEquals(1, servidor.pedidosCreados)
        assertEquals(1, servidor.sesionesPedidas)
        assertTrue(servidor.cuerpoPedido!!.contains("\"metodo_pago\":\"stripe\""), servidor.cuerpoPedido)
        assertEquals("""{"pedido_id":104}""", servidor.cuerpoSesion)
        assertEquals(1, abridor.abiertas.size)
        assertTrue(abridor.abiertas.single().startsWith("https://checkout.stripe.com/"))
        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertNull(modelo.estado.avisoPago)
        assertFalse(modelo.estado.enviando)
        assertTrue(carrito.estaVacio)
    }

    @Test
    fun sinTarjetaNoSePideNingunaSesionDePago() = runTest {
        val servidor = ServidorConTarjeta { responderJson(JSON_SESION_PAGO) }
        val abridor = AbridorFalso()
        val modelo = crearViewModel(servidor.motor, abridor = abridor)
        modelo.rellenarDomicilio()

        modelo.enviarPedido()

        assertTrue(servidor.cuerpoPedido!!.contains("\"metodo_pago\":\"efectivo_entrega\""), servidor.cuerpoPedido)
        assertEquals(0, servidor.sesionesPedidas)
        assertTrue(abridor.abiertas.isEmpty())
        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertNull(modelo.estado.avisoPago)
    }

    @Test
    fun laOpcionDeTarjetaFuncionaEnLosTresTiposDePedido() = runTest {
        val datosPorTipo = mapOf(
            TipoPedido.Mesa to { m: DatosPedidoViewModel -> m.cambiarMesa("4") },
            TipoPedido.Recoger to { m: DatosPedidoViewModel ->
                m.cambiarNombre("Pedro")
                m.cambiarTelefono("600123456")
            },
            TipoPedido.Domicilio to { m: DatosPedidoViewModel -> m.rellenarDomicilio() },
        )
        for ((tipo, rellenar) in datosPorTipo) {
            val servidor = ServidorConTarjeta { responderJson(JSON_SESION_PAGO) }
            val modelo = crearViewModel(servidor.motor)
            modelo.cambiarTipo(tipo)
            rellenar(modelo)
            modelo.cambiarPagoConTarjeta(true)

            modelo.enviarPedido()

            assertTrue(servidor.cuerpoPedido!!.contains("\"metodo_pago\":\"stripe\""), "Tipo: $tipo")
            assertEquals(1, servidor.sesionesPedidas, "Tipo: $tipo")
        }
    }

    @Test
    fun siFallaCrearSesionNoSeCreaOtroPedidoYSeGuardaElCreado() = runTest {
        val servidor = ServidorConTarjeta {
            responderJson(
                """{"success":false,"message":"La pasarela de pagos Stripe no está configurada"}""",
                HttpStatusCode.ServiceUnavailable,
            )
        }
        val abridor = AbridorFalso()
        val carrito = carritoConDosPizzas()
        val modelo = crearViewModel(servidor.motor, carrito, abridor)
        modelo.rellenarDomicilio()
        modelo.cambiarPagoConTarjeta(true)

        modelo.enviarPedido()

        val estado = modelo.estado
        assertEquals(1, servidor.pedidosCreados)
        assertEquals(104, estado.pedidoCreado?.id)
        assertTrue(carrito.estaVacio)
        assertFalse(estado.enviando)
        assertTrue(abridor.abiertas.isEmpty())
        assertTrue(
            estado.avisoPago!!.startsWith("Tu pedido está creado, pero no hemos podido iniciar el pago"),
            estado.avisoPago,
        )

        // Aunque se vuelva a pulsar "enviar", no se crea otro pedido ni otra sesión.
        modelo.enviarPedido()
        assertEquals(1, servidor.pedidosCreados)
        assertEquals(1, servidor.sesionesPedidas)
    }

    @Test
    fun siSeCaeLaRedAlCrearLaSesionTampocoSeCreaOtroPedido() = runTest {
        val servidor = ServidorConTarjeta { throw RuntimeException("Red no disponible (simulada)") }
        val modelo = crearViewModel(servidor.motor)
        modelo.rellenarDomicilio()
        modelo.cambiarPagoConTarjeta(true)

        modelo.enviarPedido()
        modelo.enviarPedido()

        assertEquals(1, servidor.pedidosCreados)
        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertTrue(modelo.estado.avisoPago!!.startsWith(AVISO_PAGO_NO_INICIADO), modelo.estado.avisoPago)
    }

    @Test
    fun siLaUrlNoEsDeStripeNoSeAbreNadaYSeAvisa() = runTest {
        val servidor = ServidorConTarjeta {
            responderJson("""{"success":true,"url":"https://evil.com/pay","sessionId":"cs_test_1"}""")
        }
        val abridor = AbridorFalso()
        val modelo = crearViewModel(servidor.motor, abridor = abridor)
        modelo.rellenarDomicilio()
        modelo.cambiarPagoConTarjeta(true)

        modelo.enviarPedido()

        assertTrue(abridor.abiertas.isEmpty())
        assertEquals(1, servidor.pedidosCreados)
        assertEquals(104, modelo.estado.pedidoCreado?.id)
        assertTrue(modelo.estado.avisoPago!!.startsWith(AVISO_PAGO_NO_INICIADO), modelo.estado.avisoPago)
        assertTrue(modelo.estado.avisoPago!!.contains(MENSAJE_URL_NO_SEGURA), modelo.estado.avisoPago)
    }
}
