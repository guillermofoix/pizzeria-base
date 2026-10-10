package es.fpmola.pizzeria.catalogo

import es.fpmola.pizzeria.modelo.Importe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Categoría de la carta (GET /api/pizzas/metadata).
 *
 * @property icono emoji de la categoría.
 */
@Serializable
data class Categoria(
    val id: Int,
    val nombre: String,
    val icono: String? = null,
)

/**
 * Ingrediente de una pizza.
 *
 * @property alergeno true si el ingrediente es alérgeno. La API no indica de
 * qué alérgeno se trata (gluten, lactosa…), solo si lo es.
 */
@Serializable
data class Ingrediente(
    val id: Int,
    val nombre: String,
    val alergeno: Boolean = false,
)

/**
 * Pizza de la carta (GET /api/pizzas).
 *
 * El precio llega unas veces como texto ("9.50") y otras como número; el tipo
 * [Importe] acepta ambos. La lista de ingredientes puede venir vacía.
 *
 * @property imagenUrl URL absoluta de la imagen (el backend no sirve imágenes).
 * @property disponible false si la pizza está agotada.
 */
@Serializable
data class Pizza(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Importe,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val disponible: Boolean = true,
    @SerialName("categoria_id") val categoriaId: Int? = null,
    @SerialName("categoria_nombre") val categoriaNombre: String? = null,
    @SerialName("categoria_icono") val categoriaIcono: String? = null,
    val ingredientes: List<Ingrediente> = emptyList(),
) {
    /** Ingredientes marcados como alérgenos (puede estar vacía). */
    val ingredientesAlergenos: List<Ingrediente>
        get() = ingredientes.filter { it.alergeno }
}

/**
 * Datos de GET /api/pizzas/metadata: todas las categorías y todos los
 * ingredientes del catálogo.
 */
@Serializable
data class MetadatosCatalogo(
    val categorias: List<Categoria> = emptyList(),
    val ingredientes: List<Ingrediente> = emptyList(),
)
