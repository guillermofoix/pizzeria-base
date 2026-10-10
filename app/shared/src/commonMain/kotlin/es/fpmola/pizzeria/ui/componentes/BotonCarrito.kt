package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Indicador del carrito para las barras de las pantallas, como la barra del
 * carrito de la web: píldora con el 🛒 y el número de unidades en una burbuja
 * roja. Abre el carrito al pulsarlo.
 */
@Composable
fun BotonCarrito(
    unidades: Int,
    alAbrir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descripcion = if (unidades == 1) {
        "Abrir el carrito, 1 unidad"
    } else {
        "Abrir el carrito, $unidades unidades"
    }
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = alAbrir)
            .semantics { contentDescription = descripcion },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "🛒", style = MaterialTheme.typography.titleMedium)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = unidades.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}
