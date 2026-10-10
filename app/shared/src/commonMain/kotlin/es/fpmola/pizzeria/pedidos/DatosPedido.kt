package es.fpmola.pizzeria.pedidos

/**
 * Tipo de pedido. Los valores de [valorApi] son los del CHECK de
 * pedidos.tipo_pedido (database/init.sql).
 *
 * @property metodoPago valor de `metodo_pago` que se envía cuando se paga en
 * el local o a la entrega. En la API es un texto libre (por defecto
 * `efectivo_entrega`); se usan los mismos valores que la web. Si se elige pagar
 * ahora con tarjeta se envía [METODO_PAGO_TARJETA] en su lugar.
 * @property etiquetaPago nombre corto de esa opción de pago, para el selector.
 * @property descripcionPago cómo se explica el pago al usuario.
 */
enum class TipoPedido(
    val valorApi: String,
    val etiqueta: String,
    val metodoPago: String,
    val etiquetaPago: String,
    val descripcionPago: String,
) {
    Mesa("mesa", "En mesa", "pago_mesa", "Pagar en la mesa", "El pago se hace en la mesa."),
    Recoger(
        "recoger", "Para recoger", "efectivo_entrega", "Efectivo al recoger",
        "El pago se hace en efectivo al recoger el pedido.",
    ),
    Domicilio(
        "domicilio", "A domicilio", "efectivo_entrega", "Efectivo al recibir",
        "El pago se hace en efectivo al recibir el pedido.",
    );

    companion object {
        /** El tipo que corresponde a un valor de la API, o null si es desconocido. */
        fun desdeApi(valor: String): TipoPedido? = entries.firstOrNull { it.valorApi == valor }
    }
}

/**
 * Lo que el usuario escribe en la pantalla de datos del pedido. Los números se
 * guardan como texto porque se están editando.
 *
 * @property pagoConTarjeta true si el usuario quiere pagar ahora con tarjeta
 * (para cualquier tipo de pedido); false es el pago habitual del tipo elegido.
 */
data class DatosPedido(
    val tipo: TipoPedido = TipoPedido.Domicilio,
    val mesa: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val observaciones: String = "",
    val pagoConTarjeta: Boolean = false,
)

/** Mensaje de error de cada campo (null si el campo es correcto o no aplica). */
data class ErroresDatos(
    val mesa: String? = null,
    val nombre: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
) {
    val hayErrores: Boolean
        get() = mesa != null || nombre != null || telefono != null || direccion != null
}

const val NOMBRE_LONGITUD_MAXIMA = 100
const val MESA_NUMERO_MAXIMO = 999
const val TELEFONO_DIGITOS_MINIMOS = 9
const val TELEFONO_DIGITOS_MAXIMOS = 15
private const val DIRECCION_LONGITUD_MINIMA = 5

/**
 * Valida solo los campos que exige cada tipo de pedido (openapi.yaml):
 * - mesa: número de mesa (el nombre es opcional).
 * - recoger: nombre y teléfono.
 * - domicilio: nombre, teléfono y dirección.
 */
fun validarDatos(datos: DatosPedido): ErroresDatos = when (datos.tipo) {
    TipoPedido.Mesa -> ErroresDatos(
        mesa = errorMesa(datos.mesa),
        nombre = errorLongitudNombre(datos.nombre),
    )
    TipoPedido.Recoger -> ErroresDatos(
        nombre = errorNombre(datos.nombre),
        telefono = errorTelefono(datos.telefono),
    )
    TipoPedido.Domicilio -> ErroresDatos(
        nombre = errorNombre(datos.nombre),
        telefono = errorTelefono(datos.telefono),
        direccion = errorDireccion(datos.direccion),
    )
}

/** Quita espacios, guiones, puntos y paréntesis de un teléfono. */
fun normalizarTelefono(texto: String): String =
    texto.filterNot { it == ' ' || it == '-' || it == '.' || it == '(' || it == ')' }

/**
 * Número de mesa como entero, o null si no es un entero entre 1 y
 * [MESA_NUMERO_MAXIMO].
 */
fun numeroDeMesa(texto: String): Int? {
    val limpio = texto.trim()
    if (limpio.isEmpty() || !limpio.all { it in '0'..'9' }) return null
    return limpio.toIntOrNull()?.takeIf { it in 1..MESA_NUMERO_MAXIMO }
}

private fun errorMesa(texto: String): String? {
    val limpio = texto.trim()
    return when {
        limpio.isEmpty() -> "Indica el número de mesa."
        !limpio.all { it in '0'..'9' } -> "El número de mesa debe ser un número entero, por ejemplo 4."
        numeroDeMesa(limpio) == null -> "El número de mesa debe estar entre 1 y $MESA_NUMERO_MAXIMO."
        else -> null
    }
}

private fun errorNombre(texto: String): String? = when {
    texto.isBlank() -> "El nombre es obligatorio."
    else -> errorLongitudNombre(texto)
}

private fun errorLongitudNombre(texto: String): String? =
    if (texto.trim().length > NOMBRE_LONGITUD_MAXIMA) {
        "El nombre es demasiado largo (máximo $NOMBRE_LONGITUD_MAXIMA caracteres)."
    } else {
        null
    }

private fun errorTelefono(texto: String): String? {
    if (texto.isBlank()) return "El teléfono es obligatorio."
    val normalizado = normalizarTelefono(texto.trim())
    val digitos = if (normalizado.startsWith("+")) normalizado.substring(1) else normalizado
    val valido = digitos.length in TELEFONO_DIGITOS_MINIMOS..TELEFONO_DIGITOS_MAXIMOS &&
        digitos.all { it in '0'..'9' }
    return if (valido) null else "Introduce un teléfono válido, por ejemplo 600 123 456."
}

private fun errorDireccion(texto: String): String? = when {
    texto.isBlank() -> "La dirección de entrega es obligatoria."
    texto.trim().length < DIRECCION_LONGITUD_MINIMA ->
        "Escribe la dirección completa (calle, número y piso)."
    else -> null
}
