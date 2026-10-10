package es.fpmola.pizzeria.pedidos

import es.fpmola.pizzeria.modelo.Importe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estado de un pedido. Los valores de [valorApi] son los del CHECK de
 * pedidos.estado (database/init.sql). El backend no tiene máquina de estados:
 * cualquier cambio es posible, pero estos tres cierran el pedido en el flujo
 * normal.
 *
 * @property esFinal true si el pedido ya no cambiará (se deja de consultar).
 */
enum class EstadoPedido(
    val valorApi: String,
    val etiqueta: String,
    val esFinal: Boolean,
) {
    Pendiente("pendiente", "Pendiente", false),
    EnPreparacion("en_preparacion", "En preparación", false),
    EnReparto("en_reparto", "En reparto", false),
    Listo("listo", "Listo", false),
    Servido("servido", "Servido", true),
    Entregado("entregado", "Entregado", true),
    Cancelado("cancelado", "Cancelado", true);

    companion object {
        /** El estado que corresponde a un valor de la API, o null si es desconocido. */
        fun desdeApi(valor: String): EstadoPedido? = entries.firstOrNull { it.valorApi == valor }
    }
}

/** Valor de `metodo_pago` del pago con tarjeta (Stripe Checkout). */
const val METODO_PAGO_TARJETA = "stripe"

/** Valor de `estado_pago` cuando el servidor ya ha confirmado el pago. */
const val ESTADO_PAGO_PAGADO = "pagado"

/**
 * Línea del cuerpo de POST /api/pedidos. Solo lleva el id de la pizza (que
 * viene de la carta cargada), la cantidad y notas: el servidor ignora
 * cualquier precio, así que la app no envía ninguno.
 */
@Serializable
data class LineaPeticion(
    @SerialName("pizza_id") val pizzaId: Int,
    val cantidad: Int,
    val notas: String? = null,
)

/**
 * Cuerpo de POST /api/pedidos (openapi.yaml). Los campos con valor por defecto
 * (null) no se envían.
 */
@Serializable
data class PeticionPedido(
    @SerialName("tipo_pedido") val tipoPedido: String,
    @SerialName("mesa_numero") val mesaNumero: Int? = null,
    @SerialName("cliente_nombre") val clienteNombre: String? = null,
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    @SerialName("cliente_direccion") val clienteDireccion: String? = null,
    @SerialName("metodo_pago") val metodoPago: String,
    val observaciones: String? = null,
    val lineas: List<LineaPeticion>,
)

/**
 * Línea de un pedido tal como la devuelve el servidor. Sirve para la respuesta
 * de crear el pedido (sin `linea_id`, `imagen_url` ni `subtotal`) y para
 * GET /api/pedidos/{id} (con ellos).
 */
@Serializable
data class LineaPedido(
    @SerialName("linea_id") val lineaId: Int? = null,
    @SerialName("pizza_id") val pizzaId: Int,
    val nombre: String,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val cantidad: Int,
    @SerialName("precio_unitario") val precioUnitario: Importe,
    val subtotal: Importe? = null,
    val notas: String? = null,
) {
    /** El subtotal del servidor o, si no viene, precio unitario por cantidad. */
    val subtotalCalculado: Importe
        get() = subtotal ?: (precioUnitario * cantidad)
}

/**
 * Pedido tal como lo devuelve el servidor (POST y GET /api/pedidos/{id}).
 * El tipo y el estado se guardan como texto para no fallar con valores nuevos;
 * [tipo] y [estadoConocido] los convierten cuando se reconocen.
 */
@Serializable
data class Pedido(
    val id: Int,
    @SerialName("tipo_pedido") val tipoPedido: String,
    @SerialName("mesa_numero") val mesaNumero: Int? = null,
    val fecha: String? = null,
    val estado: String,
    val total: Importe,
    @SerialName("cliente_nombre") val clienteNombre: String? = null,
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    @SerialName("cliente_direccion") val clienteDireccion: String? = null,
    @SerialName("metodo_pago") val metodoPago: String? = null,
    @SerialName("estado_pago") val estadoPago: String? = null,
    val observaciones: String? = null,
    val lineas: List<LineaPedido> = emptyList(),
) {
    val tipo: TipoPedido?
        get() = TipoPedido.desdeApi(tipoPedido)

    val estadoConocido: EstadoPedido?
        get() = EstadoPedido.desdeApi(estado)

    /** true si el estado es uno de los finales (un estado desconocido no lo es). */
    val esFinal: Boolean
        get() = estadoConocido?.esFinal == true

    /** true si el pedido se paga con tarjeta (Stripe). */
    val pagoConTarjeta: Boolean
        get() = metodoPago == METODO_PAGO_TARJETA

    /** true si el servidor ya ha confirmado el pago. */
    val pagado: Boolean
        get() = estadoPago == ESTADO_PAGO_PAGADO

    /**
     * true si es un pedido con tarjeta que todavía hay que pagar. Un pedido
     * cancelado no se paga, así que no cuenta como pendiente.
     */
    val pagoConTarjetaPendiente: Boolean
        get() = pagoConTarjeta && !pagado && estadoConocido != EstadoPedido.Cancelado
}
