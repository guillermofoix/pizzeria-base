package es.fpmola.pizzeria.modelo

import es.fpmola.pizzeria.red.jsonApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable

class ImporteTest {

    private fun leer(json: String): Importe = jsonApi.decodeFromString(ImporteSerializador, json)

    // --- Número JSON ---------------------------------------------------------

    @Test
    fun numeroEntero() = assertEquals(1200L, leer("12").centimos)

    @Test
    fun numeroConUnDecimal() = assertEquals(1250L, leer("12.5").centimos)

    @Test
    fun numeroConDosDecimales() = assertEquals(1350L, leer("13.50").centimos)

    @Test
    fun numeroConErrorDeComaFlotanteDeJavaScript() =
        assertEquals(2550L, leer("25.500000000000004").centimos)

    // --- Texto JSON ----------------------------------------------------------

    @Test
    fun textoNumeric() = assertEquals(950L, leer("\"9.50\"").centimos)

    @Test
    fun textoCentimos() = assertEquals(5L, leer("\"0.05\"").centimos)

    @Test
    fun textoNegativo() = assertEquals(-310L, leer("\"-3.10\"").centimos)

    @Test
    fun textoConComaDecimal() = assertEquals(1250L, leer("\"12,50\"").centimos)

    @Test
    fun textoConEspaciosYCerosIzquierda() = assertEquals(750L, leer("\" 007.5 \"").centimos)

    @Test
    fun masDeDosDecimalesRedondeaMitadHaciaArriba() {
        assertEquals(1000L, leer("\"9.995\"").centimos)
        assertEquals(999L, leer("\"9.994\"").centimos)
        assertEquals(-1000L, leer("\"-9.995\"").centimos)
    }

    // --- Formatos no válidos -------------------------------------------------

    @Test
    fun rechazaFormatosRaros() {
        val noValidos = listOf(
            "\"\"", "\"abc\"", "\"12.\"", "\".5\"", "\"12.5.3\"", "\"1e3\"",
            "\"12 €\"", "\"--1\"", "\"+\"", "1e3", "null", "true", "[]", "{}",
        )
        for (json in noValidos) {
            assertFailsWith<SerializationException>("Debería rechazar $json") { leer(json) }
        }
    }

    @Test
    fun rechazaImportesDemasiadoGrandes() {
        assertFailsWith<SerializationException> { leer("\"1234567890123456.00\"") }
    }

    // --- Salida --------------------------------------------------------------

    @Test
    fun formatoParaElUsuario() {
        assertEquals("12,50 €", Importe(1250).formatear())
        assertEquals("0,05 €", Importe(5).formatear())
        assertEquals("0,00 €", Importe.CERO.formatear())
        assertEquals("-3,10 €", Importe(-310).formatear())
        assertEquals("1234,56 €", Importe(123456).formatear())
    }

    @Test
    fun serializaComoTextoDeLaApi() {
        assertEquals("\"12.50\"", jsonApi.encodeToString(ImporteSerializador, Importe(1250)))
        assertEquals("\"-0.05\"", jsonApi.encodeToString(ImporteSerializador, Importe(-5)))
    }

    @Test
    fun comparaPorCentimos() {
        assertTrue(Importe(100) < Importe(101))
        assertEquals(Importe(1250), leer("12.5"))
    }

    // --- Dentro de un objeto -------------------------------------------------

    @Serializable
    private data class Linea(val precio: Importe, val subtotal: Importe)

    @Test
    fun dentroDeUnObjetoAceptaNumeroYTexto() {
        val linea = jsonApi.decodeFromString(
            Linea.serializer(),
            """{"precio":"12.00","subtotal":24}""",
        )
        assertEquals(Importe(1200), linea.precio)
        assertEquals(Importe(2400), linea.subtotal)
    }
}
