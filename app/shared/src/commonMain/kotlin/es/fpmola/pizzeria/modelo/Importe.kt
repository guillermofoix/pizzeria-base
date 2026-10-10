package es.fpmola.pizzeria.modelo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Importe en euros guardado como céntimos enteros (sin coma flotante).
 *
 * La API devuelve los importes unas veces como texto ("12.50", columnas NUMERIC)
 * y otras como número JSON (12.5); el serializador acepta ambos.
 */
@Serializable(with = ImporteSerializador::class)
data class Importe(val centimos: Long) : Comparable<Importe> {

    override fun compareTo(other: Importe): Int = centimos.compareTo(other.centimos)

    /** Suma de dos importes (en céntimos, sin decimales). */
    operator fun plus(otro: Importe): Importe = Importe(centimos + otro.centimos)

    /** Importe multiplicado por un número de unidades. */
    operator fun times(unidades: Int): Importe = Importe(centimos * unidades)

    /** Formato para el usuario: "12,50 €". */
    fun formatear(): String = "${textoConSeparador(',')} €"

    /** Formato de la API: "12.50". */
    fun aTextoApi(): String = textoConSeparador('.')

    private fun textoConSeparador(separador: Char): String {
        val signo = if (centimos < 0) "-" else ""
        // Se trabaja con cociente y resto para no desbordar con Long.MIN_VALUE.
        val euros = (centimos / 100).let { if (it < 0) -it else it }
        val resto = (centimos % 100).let { if (it < 0) -it else it }
        return "$signo$euros$separador${resto.toString().padStart(2, '0')}"
    }

    companion object {
        /** Máximo de cifras enteras admitidas (cabe de sobra en Long en céntimos). */
        private const val MAX_CIFRAS_ENTERAS = 15

        val CERO = Importe(0)

        /**
         * Convierte un texto decimal ("12.50", "12,5", "-3", "25.500000000000004")
         * en céntimos. Acepta punto o coma decimal y signo opcional. Con más de dos
         * decimales redondea al céntimo (mitad hacia arriba, en valor absoluto).
         * Rechaza notación exponencial, texto vacío y cualquier otro carácter.
         *
         * @throws IllegalArgumentException si el texto no es un importe válido.
         */
        fun desdeTexto(texto: String): Importe {
            val limpio = texto.trim()
            require(limpio.isNotEmpty()) { "Importe vacío" }

            val negativo = limpio[0] == '-'
            val sinSigno = if (limpio[0] == '-' || limpio[0] == '+') limpio.substring(1) else limpio

            val posSeparador = sinSigno.indexOfFirst { it == '.' || it == ',' }
            val parteEntera = if (posSeparador < 0) sinSigno else sinSigno.substring(0, posSeparador)
            val parteDecimal = if (posSeparador < 0) "" else sinSigno.substring(posSeparador + 1)

            require(parteEntera.isNotEmpty() && parteEntera.all(::esCifra)) {
                "Importe no válido: \"$texto\""
            }
            require(posSeparador < 0 || (parteDecimal.isNotEmpty() && parteDecimal.all(::esCifra))) {
                "Importe no válido: \"$texto\""
            }
            require(parteEntera.trimStart('0').length <= MAX_CIFRAS_ENTERAS) {
                "Importe demasiado grande: \"$texto\""
            }

            val euros = parteEntera.toLong()
            val centimosDecimales = parteDecimal.padEnd(2, '0').take(2).toLong()
            val redondeo = if (parteDecimal.length > 2 && parteDecimal[2] >= '5') 1 else 0
            val total = euros * 100 + centimosDecimales + redondeo

            return Importe(if (negativo) -total else total)
        }

        private fun esCifra(c: Char): Boolean = c in '0'..'9'
    }
}

/**
 * Serializador de [Importe]: lee un número JSON o un texto y escribe texto "12.50".
 */
object ImporteSerializador : KSerializer<Importe> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("es.fpmola.pizzeria.modelo.Importe", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Importe {
        val texto = if (decoder is JsonDecoder) {
            val elemento = decoder.decodeJsonElement()
            if (elemento !is JsonPrimitive || elemento is JsonNull) {
                throw SerializationException("Se esperaba un importe (número o texto) y se recibió: $elemento")
            }
            // Para números y textos, content es el literal tal cual llegó ("12.5" o "12.50").
            elemento.content
        } else {
            decoder.decodeString()
        }

        return try {
            Importe.desdeTexto(texto)
        } catch (e: IllegalArgumentException) {
            throw SerializationException(e.message ?: "Importe no válido")
        }
    }

    override fun serialize(encoder: Encoder, value: Importe) {
        encoder.encodeString(value.aTextoApi())
    }
}
