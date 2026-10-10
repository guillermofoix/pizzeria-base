package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tarjeta como las de la web: color de tarjeta del tema (nunca el de Material
 * por defecto), borde fino y una sombra suave. Si se indica [alPulsar] es pulsable.
 */
@Composable
fun TarjetaPizzeria(
    modifier: Modifier = Modifier,
    alPulsar: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val esquema = MaterialTheme.colorScheme
    val colores = CardDefaults.cardColors(
        containerColor = esquema.surfaceContainer,
        contentColor = esquema.onSurface,
    )
    val elevacion = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val borde = BorderStroke(1.dp, esquema.outlineVariant)

    if (alPulsar != null) {
        Card(
            onClick = alPulsar,
            modifier = modifier,
            colors = colores,
            elevation = elevacion,
            border = borde,
            content = contenido,
        )
    } else {
        Card(
            modifier = modifier,
            colors = colores,
            elevation = elevacion,
            border = borde,
            content = contenido,
        )
    }
}
