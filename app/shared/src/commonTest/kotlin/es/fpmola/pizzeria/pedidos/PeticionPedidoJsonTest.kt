package es.fpmola.pizzeria.pedidos

import es.fpmola.pizzeria.carrito.LineaCarrito
import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.red.jsonApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.Json

class PeticionPedidoJsonTest {

    private fun json(peticion: PeticionPedido): String =
        jsonApi.encodeToString(PeticionPedido.serializer(), peticion)

    private fun lineaCarrito(pizzaId: Int, cantidad: Int) = LineaCarrito(
        pizzaId = pizzaId,
        nombre = "Pizza $pizzaId",
        imagenUrl = null,
        precioUnitario = Importe(1000),
        cantidad = cantidad,
    )

    /** Compara dos JSON sin depender del orden de los campos ni de los espacios. */
    private fun assertJsonIgual(esperado: String, real: String) {
        assertEquals(Json.parseToJsonElement(esperado), Json.parseToJsonElement(real))
    }

    @Test
    fun serializaEnSnakeCaseComoElEjemploDeOpenapi() {
        // Ejemplo "domicilio" de POST /api/pedidos en openapi.yaml.
        val peticion = PeticionPedido(
            tipoPedido = "domicilio",
            clienteNombre = "Ana López",
            clienteTelefono = "600123456",
            clienteDireccion = "Calle Mayor 1, 2º A",
            metodoPago = "stripe",
            observaciones = "Llamar al timbre",
            lineas = listOf(
                LineaPeticion(pizzaId = 1, cantidad = 2),
                LineaPeticion(pizzaId = 3, cantidad = 1, notas = "Sin gorgonzola"),
            ),
        )

        assertJsonIgual(
            """
            {
              "tipo_pedido": "domicilio",
              "cliente_nombre": "Ana López",
              "cliente_telefono": "600123456",
              "cliente_direccion": "Calle Mayor 1, 2º A",
              "metodo_pago": "stripe",
              "observaciones": "Llamar al timbre",
              "lineas": [
                { "pizza_id": 1, "cantidad": 2 },
                { "pizza_id": 3, "cantidad": 1, "notas": "Sin gorgonzola" }
              ]
            }
            """.trimIndent(),
            json(peticion),
        )
    }

    @Test
    fun pedidoDeMesaCoincideConElEjemploDeOpenapi() {
        // Ejemplo "mesa" de openapi.yaml: mesa 4, pizza 6 × 1, pago_mesa.
        val peticion = construirPeticion(
            DatosPedido(tipo = TipoPedido.Mesa, mesa = "4"),
            listOf(lineaCarrito(pizzaId = 6, cantidad = 1)),
        )

        assertJsonIgual(
            """
            {
              "tipo_pedido": "mesa",
              "mesa_numero": 4,
              "metodo_pago": "pago_mesa",
              "lineas": [ { "pizza_id": 6, "cantidad": 1 } ]
            }
            """.trimIndent(),
            json(peticion),
        )
    }

    @Test
    fun pedidoParaRecogerSoloLlevaNombreYTelefono() {
        val peticion = construirPeticion(
            DatosPedido(
                tipo = TipoPedido.Recoger,
                nombre = " Pedro ",
                telefono = "600 000 003",
                // Estos campos no corresponden al tipo y no deben enviarse.
                mesa = "9",
                direccion = "Calle Falsa 123",
            ),
            listOf(lineaCarrito(pizzaId = 2, cantidad = 1)),
        )

        assertJsonIgual(
            """
            {
              "tipo_pedido": "recoger",
              "cliente_nombre": "Pedro",
              "cliente_telefono": "600000003",
              "metodo_pago": "efectivo_entrega",
              "lineas": [ { "pizza_id": 2, "cantidad": 1 } ]
            }
            """.trimIndent(),
            json(peticion),
        )
    }

    @Test
    fun pedidoADomicilioLlevaDireccionYObservaciones() {
        val peticion = construirPeticion(
            DatosPedido(
                tipo = TipoPedido.Domicilio,
                nombre = "Ana López",
                telefono = "600-123-456",
                direccion = "  Calle Mayor 1, 2º A ",
                observaciones = "Llamar al timbre",
            ),
            listOf(
                lineaCarrito(pizzaId = 1, cantidad = 2),
                lineaCarrito(pizzaId = 3, cantidad = 1),
            ),
        )

        assertJsonIgual(
            """
            {
              "tipo_pedido": "domicilio",
              "cliente_nombre": "Ana López",
              "cliente_telefono": "600123456",
              "cliente_direccion": "Calle Mayor 1, 2º A",
              "metodo_pago": "efectivo_entrega",
              "observaciones": "Llamar al timbre",
              "lineas": [
                { "pizza_id": 1, "cantidad": 2 },
                { "pizza_id": 3, "cantidad": 1 }
              ]
            }
            """.trimIndent(),
            json(peticion),
        )
    }

    @Test
    fun laPeticionNuncaLlevaPrecios() {
        val texto = json(
            construirPeticion(
                DatosPedido(tipo = TipoPedido.Mesa, mesa = "1"),
                listOf(lineaCarrito(pizzaId = 1, cantidad = 3)),
            ),
        )

        // El servidor recalcula los precios: la app no envía ninguno.
        assertFalse(texto.contains("precio"), texto)
        assertFalse(texto.contains("total"), texto)
        assertFalse(texto.contains("subtotal"), texto)
    }

    @Test
    fun elTelefonoSeEnviaComoTexto() {
        val texto = json(
            construirPeticion(
                DatosPedido(tipo = TipoPedido.Recoger, nombre = "Pedro", telefono = "600000003"),
                listOf(lineaCarrito(pizzaId = 1, cantidad = 1)),
            ),
        )

        assertEquals(true, texto.contains("\"cliente_telefono\":\"600000003\""), texto)
    }

    @Test
    fun cadaTipoUsaSuMetodoDePago() {
        assertEquals("pago_mesa", TipoPedido.Mesa.metodoPago)
        assertEquals("efectivo_entrega", TipoPedido.Recoger.metodoPago)
        assertEquals("efectivo_entrega", TipoPedido.Domicilio.metodoPago)
    }

    @Test
    fun losValoresDelTipoSonLosDelCheckDeLaBaseDeDatos() {
        assertEquals(
            listOf("mesa", "recoger", "domicilio"),
            TipoPedido.entries.map { it.valorApi },
        )
    }

    @Test
    fun losEstadosSonLosDelCheckDeLaBaseDeDatos() {
        assertEquals(
            listOf(
                "pendiente", "en_preparacion", "en_reparto", "listo",
                "servido", "entregado", "cancelado",
            ),
            EstadoPedido.entries.map { it.valorApi },
        )
        assertEquals(
            listOf("servido", "entregado", "cancelado"),
            EstadoPedido.entries.filter { it.esFinal }.map { it.valorApi },
        )
    }
}
