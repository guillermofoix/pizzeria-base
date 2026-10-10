package es.fpmola.pizzeria.ui.pedidos

import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.JSON_PEDIDO_CREADO
import es.fpmola.pizzeria.prueba.carritoConDosPizzas
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.responderJson
import io.ktor.client.engine.mock.MockEngine
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

    private fun crearViewModel(motor: MockEngine, carrito: Carrito = carritoConDosPizzas()) =
        DatosPedidoViewModel(
            repositorio = RepositorioPedidos(crearClientePrueba(AjustesEnMemoria(), motor)),
            carrito = carrito,
        )

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
}
