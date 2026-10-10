package es.fpmola.pizzeria.prueba

/**
 * Ejemplos de respuestas del backend para los tests del catálogo, basados en
 * los ejemplos de openapi.yaml.
 *
 * Reparto de la carta: la categoría 1 tiene las pizzas 1 y 2, la categoría 2
 * la pizza 4, la categoría 3 ninguna y la pizza 3 no tiene categoría.
 */

/**
 * GET /api/pizzas. Cubre: precio como texto (1, 3, 4) y como número (2),
 * ingredientes vacíos (2), pizza agotada sin imagen ni categoría (3) y
 * campos desconocidos (count, creado_en).
 */
const val JSON_PIZZAS = """
{
  "success": true,
  "count": 4,
  "data": [
    {
      "id": 1,
      "nombre": "Margherita Clásica",
      "descripcion": "La auténtica reina de Nápoles: salsa de tomate San Marzano, mozzarella Fior di Latte fresca, hojas de albahaca fresca y aceite de oliva virgen extra.",
      "precio": "9.50",
      "imagen_url": "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=800&q=80",
      "disponible": true,
      "categoria_id": 1,
      "categoria_nombre": "Clásicas",
      "categoria_icono": "🍕",
      "creado_en": "2026-10-10T09:00:00.000Z",
      "ingredientes": [
        { "id": 1, "nombre": "Salsa de tomate", "alergeno": false },
        { "id": 2, "nombre": "Mozzarella", "alergeno": true }
      ]
    },
    {
      "id": 2,
      "nombre": "Diávolo Pepperoni",
      "descripcion": "Para los amantes del toque picante.",
      "precio": 12,
      "imagen_url": "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=800&q=80",
      "disponible": true,
      "categoria_id": 1,
      "categoria_nombre": "Clásicas",
      "categoria_icono": "🍕",
      "ingredientes": []
    },
    {
      "id": 3,
      "nombre": "Hawaiana Especial",
      "descripcion": null,
      "precio": "11.50",
      "imagen_url": null,
      "disponible": false,
      "categoria_id": null,
      "categoria_nombre": null,
      "categoria_icono": null,
      "ingredientes": [
        { "id": 2, "nombre": "Mozzarella", "alergeno": true },
        { "id": 3, "nombre": "Piña", "alergeno": false }
      ]
    },
    {
      "id": 4,
      "nombre": "Barbacoa Texas Crunch",
      "descripcion": "Salsa barbacoa ahumada artesanal.",
      "precio": "14.00",
      "imagen_url": "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=800&q=80",
      "disponible": true,
      "categoria_id": 2,
      "categoria_nombre": "Especiales",
      "categoria_icono": "⭐",
      "ingredientes": [
        { "id": 2, "nombre": "Mozzarella", "alergeno": true }
      ]
    }
  ]
}
"""

/** GET /api/pizzas/metadata (ejemplo de openapi.yaml). */
const val JSON_METADATOS = """
{
  "success": true,
  "data": {
    "categorias": [
      { "id": 1, "nombre": "Clásicas", "icono": "🍕" },
      { "id": 2, "nombre": "Especiales", "icono": "⭐" },
      { "id": 3, "nombre": "Gourmet", "icono": "👑" },
      { "id": 4, "nombre": "Bebidas y Postres", "icono": "🥤" }
    ],
    "ingredientes": [
      { "id": 7, "nombre": "Albahaca fresca", "alergeno": false },
      { "id": 2, "nombre": "Mozzarella", "alergeno": true }
    ]
  }
}
"""
