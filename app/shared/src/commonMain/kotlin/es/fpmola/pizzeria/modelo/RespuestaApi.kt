package es.fpmola.pizzeria.modelo

import kotlinx.serialization.Serializable

/**
 * Envoltorio común de las respuestas del backend: { success, data, message, error }.
 *
 * Los nombres de las propiedades coinciden con los campos de la API (contrato).
 * Otros campos que pueda traer la respuesta (por ejemplo `count`) se ignoran.
 *
 * @property success true si la operación fue correcta.
 * @property data contenido de la respuesta; puede faltar (por ejemplo en errores).
 * @property message mensaje legible del servidor.
 * @property error mensaje técnico interno del servidor (solo en algunos errores).
 */
@Serializable
data class RespuestaApi<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null,
)
