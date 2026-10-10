package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.pedidos.EstadoPedido
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.ui.tema.coloresPizzeria

/** Emoji de cada tipo de pedido, los mismos que usa la web. */
val TipoPedido.emoji: String
    get() = when (this) {
        TipoPedido.Mesa -> "🍽️"
        TipoPedido.Recoger -> "🥡"
        TipoPedido.Domicilio -> "🛵"
    }

/**
 * Cómo se muestra un estado de pedido: emoji, color y avance de la barra de
 * progreso (null si no tiene barra).
 */
class PresentacionEstado(
    val emoji: String,
    val color: Color,
    val avance: Float?,
)

/**
 * Emoji, color y avance de un estado, según la web. Los avances son los de la
 * barra de progreso de la web de clientes (25 %, 60 %, 85 %, 90 % y 100 %).
 * Un estado desconocido (null) se muestra en gris y sin barra.
 */
@Composable
fun presentacionEstado(estado: EstadoPedido?): PresentacionEstado {
    val colores = MaterialTheme.coloresPizzeria
    return when (estado) {
        EstadoPedido.Pendiente -> PresentacionEstado("⏳", colores.estadoPendiente, 0.25f)
        EstadoPedido.EnPreparacion -> PresentacionEstado("🔥", colores.estadoPreparacion, 0.60f)
        EstadoPedido.EnReparto -> PresentacionEstado("🛵", colores.estadoReparto, 0.85f)
        EstadoPedido.Listo -> PresentacionEstado("✅", colores.estadoListo, 0.90f)
        EstadoPedido.Servido -> PresentacionEstado("✅", colores.estadoListo, 1f)
        EstadoPedido.Entregado -> PresentacionEstado("📦", colores.estadoEntregado, 1f)
        EstadoPedido.Cancelado -> PresentacionEstado("❌", colores.estadoCancelado, null)
        null -> PresentacionEstado("", colores.estadoEntregado, null)
    }
}

/**
 * Insignia de estado como la de la web: fondo del color al 10 %, texto del
 * color y borde del color al 20 %.
 */
@Composable
fun InsigniaEstado(
    texto: String,
    presentacion: PresentacionEstado,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = presentacion.color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, presentacion.color.copy(alpha = 0.20f)),
    ) {
        Text(
            text = listOf(presentacion.emoji, texto).filter { it.isNotEmpty() }.joinToString(" "),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = presentacion.color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Barra de progreso del pedido, del color del estado. */
@Composable
fun BarraProgresoPedido(
    avance: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { avance },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}
