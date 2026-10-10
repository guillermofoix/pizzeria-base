package es.fpmola.pizzeria.ui.pedidos

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.pedidos.EstadoPedido
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.prueba.AjustesEnMemoria
import es.fpmola.pizzeria.prueba.crearClientePrueba
import es.fpmola.pizzeria.prueba.jsonPedido
import es.fpmola.pizzeria.prueba.responderJson
import es.fpmola.pizzeria.red.ErrorRed
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PedidoConfirmadoViewModelTest {

    private fun crearViewModel(motor: MockEngine, pedidoInicial: Pedido? = null) =
        PedidoConfirmadoViewModel(
            repositorio = RepositorioPedidos(crearClientePrueba(AjustesEnMemoria(), motor)),
            pedidoId = 104,
            pedidoInicial = pedidoInicial,
        )

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
}
