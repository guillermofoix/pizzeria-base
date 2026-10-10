package es.fpmola.pizzeria.catalogo

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.modelo.RespuestaApi
import es.fpmola.pizzeria.prueba.JSON_METADATOS
import es.fpmola.pizzeria.prueba.JSON_PIZZAS
import es.fpmola.pizzeria.red.jsonApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer

class CatalogoJsonTest {

    private fun leerPizzas(json: String): List<Pizza> {
        val respuesta = jsonApi.decodeFromString(
            RespuestaApi.serializer(ListSerializer(Pizza.serializer())),
            json,
        )
        assertTrue(respuesta.success)
        return respuesta.data ?: error("La respuesta no trae data")
    }

    @Test
    fun leeLaCartaCompletaIgnorandoCamposDesconocidos() {
        val pizzas = leerPizzas(JSON_PIZZAS)

        assertEquals(listOf(1, 2, 3, 4), pizzas.map { it.id })
        val margherita = pizzas[0]
        assertEquals("Margherita Clásica", margherita.nombre)
        assertEquals(1, margherita.categoriaId)
        assertEquals("Clásicas", margherita.categoriaNombre)
        assertEquals("🍕", margherita.categoriaIcono)
        assertTrue(margherita.disponible)
        assertTrue(margherita.imagenUrl!!.startsWith("https://images.unsplash.com/"))
    }

    @Test
    fun elPrecioPuedeVenirComoTextoOComoNumero() {
        val pizzas = leerPizzas(JSON_PIZZAS)

        assertEquals(Importe(950), pizzas[0].precio)   // "9.50"
        assertEquals(Importe(1200), pizzas[1].precio)  // 12
        assertEquals(Importe(1150), pizzas[2].precio)  // "11.50"
        assertEquals("12,00 €", pizzas[1].precio.formatear())
    }

    @Test
    fun losIngredientesPuedenVenirVacios() {
        val diavolo = leerPizzas(JSON_PIZZAS)[1]

        assertTrue(diavolo.ingredientes.isEmpty())
        assertTrue(diavolo.ingredientesAlergenos.isEmpty())
    }

    @Test
    fun leeIngredientesYAlergenos() {
        val margherita = leerPizzas(JSON_PIZZAS)[0]

        assertEquals(listOf("Salsa de tomate", "Mozzarella"), margherita.ingredientes.map { it.nombre })
        assertEquals(listOf("Mozzarella"), margherita.ingredientesAlergenos.map { it.nombre })
    }

    @Test
    fun admiteCamposNulosEnUnaPizzaAgotada() {
        val hawaiana = leerPizzas(JSON_PIZZAS)[2]

        assertFalse(hawaiana.disponible)
        assertNull(hawaiana.descripcion)
        assertNull(hawaiana.imagenUrl)
        assertNull(hawaiana.categoriaId)
        assertNull(hawaiana.categoriaNombre)
        assertNull(hawaiana.categoriaIcono)
    }

    @Test
    fun usaValoresPorDefectoSiFaltanCamposOpcionales() {
        val pizzas = leerPizzas("""{"success":true,"data":[{"id":9,"nombre":"Mínima","precio":"1.00"}]}""")

        val pizza = pizzas.single()
        assertTrue(pizza.disponible)
        assertTrue(pizza.ingredientes.isEmpty())
        assertNull(pizza.imagenUrl)
    }

    @Test
    fun leeLosMetadatosDelCatalogo() {
        val respuesta = jsonApi.decodeFromString(
            RespuestaApi.serializer(MetadatosCatalogo.serializer()),
            JSON_METADATOS,
        )

        val metadatos = respuesta.data ?: error("La respuesta no trae data")
        assertEquals(
            listOf("Clásicas", "Especiales", "Gourmet", "Bebidas y Postres"),
            metadatos.categorias.map { it.nombre },
        )
        assertEquals("👑", metadatos.categorias[2].icono)
        assertEquals(2, metadatos.ingredientes.size)
        assertTrue(metadatos.ingredientes.single { it.nombre == "Mozzarella" }.alergeno)
    }
}
