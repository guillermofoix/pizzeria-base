package es.fpmola.pizzeria.modelo

import es.fpmola.pizzeria.red.jsonApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

class RespuestaApiTest {

    @Serializable
    private data class PizzaPrueba(val id: Int, val nombre: String, val precio: Importe)

    private val serializadorLista = RespuestaApi.serializer(ListSerializer(PizzaPrueba.serializer()))

    @Test
    fun leeUnaListaIgnorandoCamposDesconocidos() {
        // Ejemplo real de GET /api/pizzas (recortado): trae "count" y más campos.
        val json = """
            {
              "success": true,
              "count": 2,
              "data": [
                {"id": 1, "nombre": "Margherita Clásica", "precio": "9.50", "disponible": true,
                 "ingredientes": [{"id": 2, "nombre": "Mozzarella", "alergeno": true}]},
                {"id": 2, "nombre": "Diávolo Pepperoni", "precio": "12.00", "disponible": true}
              ]
            }
        """.trimIndent()

        val respuesta = jsonApi.decodeFromString(serializadorLista, json)

        assertTrue(respuesta.success)
        assertNull(respuesta.message)
        assertEquals(2, respuesta.data?.size)
        assertEquals("Margherita Clásica", respuesta.data?.get(0)?.nombre)
        assertEquals(Importe(950), respuesta.data?.get(0)?.precio)
        assertEquals(Importe(1200), respuesta.data?.get(1)?.precio)
    }

    @Test
    fun leeUnErrorSinData() {
        val json = """{"success": false, "message": "Pedido #999 no encontrado"}"""

        val respuesta = jsonApi.decodeFromString(serializadorLista, json)

        assertFalse(respuesta.success)
        assertNull(respuesta.data)
        assertEquals("Pedido #999 no encontrado", respuesta.message)
        assertNull(respuesta.error)
    }

    @Test
    fun leeElErrorInterno() {
        val json = """
            {"success": false, "message": "Error interno al consultar pedidos",
             "error": "invalid input syntax for type integer"}
        """.trimIndent()

        val respuesta = jsonApi.decodeFromString(serializadorLista, json)

        assertEquals("invalid input syntax for type integer", respuesta.error)
    }
}
