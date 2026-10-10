package es.fpmola.pizzeria.prueba

import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.modelo.Importe

/** Pizza de prueba con el id, el precio en céntimos y la disponibilidad indicados. */
fun pizzaPrueba(
    id: Int,
    precioCentimos: Long = 1000,
    disponible: Boolean = true,
    nombre: String = "Pizza $id",
): Pizza = Pizza(
    id = id,
    nombre = nombre,
    precio = Importe(precioCentimos),
    disponible = disponible,
)

/** Carrito con dos pizzas: 2 × 9,50 € (id 1) y 1 × 13,50 € (id 3). */
fun carritoConDosPizzas(): Carrito = Carrito().apply {
    anadir(pizzaPrueba(id = 1, precioCentimos = 950, nombre = "Margherita Clásica"), 2)
    anadir(pizzaPrueba(id = 3, precioCentimos = 1350, nombre = "Cuatro Quesos Cremosa"), 1)
}

/**
 * Respuesta de POST /api/pedidos (ejemplo de openapi.yaml): pedido 104 a
 * domicilio con importes de las líneas como número.
 */
const val JSON_PEDIDO_CREADO = """
{
  "success": true,
  "message": "¡Pedido enviado a cocina con éxito!",
  "data": {
    "id": 104,
    "tipo_pedido": "domicilio",
    "mesa_numero": null,
    "fecha": "2026-10-10T10:20:00.000Z",
    "estado": "pendiente",
    "total": "32.50",
    "cliente_nombre": "Ana López",
    "cliente_telefono": "600123456",
    "cliente_direccion": "Calle Mayor 1, 2º A",
    "metodo_pago": "efectivo_entrega",
    "estado_pago": "pendiente",
    "stripe_session_id": null,
    "observaciones": "Llamar al timbre",
    "lineas": [
      { "pizza_id": 1, "nombre": "Margherita Clásica", "cantidad": 2, "precio_unitario": 9.5, "notas": null },
      { "pizza_id": 3, "nombre": "Cuatro Quesos Cremosa", "cantidad": 1, "precio_unitario": 13.5, "notas": "Sin gorgonzola" }
    ]
  }
}
"""

/** Respuesta de POST /api/pedidos de un pedido pagado con tarjeta (metodo_pago stripe). */
val JSON_PEDIDO_CREADO_TARJETA: String = JSON_PEDIDO_CREADO.replace("efectivo_entrega", "stripe")

/** Respuesta de POST /api/pagos/crear-sesion (sin envoltorio {success,data}). */
const val JSON_SESION_PAGO = """
{
  "success": true,
  "url": "https://checkout.stripe.com/c/pay/cs_test_a1B2c3#fidkdWxOYHwnPyd1blpxYHZxWjA0",
  "sessionId": "cs_test_a1B2c3"
}
"""

/** Respuesta de GET /api/pedidos/104 con el estado indicado. */
fun jsonPedido(
    estado: String,
    estadoPago: String = "pendiente",
    metodoPago: String = "efectivo_entrega",
): String = """
{
  "success": true,
  "data": {
    "id": 104,
    "tipo_pedido": "domicilio",
    "mesa_numero": null,
    "fecha": "2026-10-10T10:20:00.000Z",
    "estado": "$estado",
    "total": "32.50",
    "cliente_nombre": "Ana López",
    "cliente_telefono": "600123456",
    "cliente_direccion": "Calle Mayor 1, 2º A",
    "metodo_pago": "$metodoPago",
    "estado_pago": "$estadoPago",
    "stripe_session_id": null,
    "observaciones": "Llamar al timbre",
    "lineas": [
      { "linea_id": 1, "pizza_id": 1, "nombre": "Margherita Clásica",
        "imagen_url": "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=800&q=80",
        "cantidad": 2, "precio_unitario": 9.5, "subtotal": 19, "notas": null },
      { "linea_id": 2, "pizza_id": 3, "nombre": "Cuatro Quesos Cremosa", "imagen_url": null,
        "cantidad": 1, "precio_unitario": 13.5, "subtotal": 13.5, "notas": "Sin gorgonzola" }
    ]
  }
}
"""
