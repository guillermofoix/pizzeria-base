package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Chip en forma de píldora como los de la web: el seleccionado en rojo con
 * texto blanco y el resto en el color de tarjeta con borde fino y texto atenuado.
 */
@Composable
fun ChipPizzeria(
    seleccionado: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    etiqueta: @Composable () -> Unit,
) {
    val esquema = MaterialTheme.colorScheme
    FilterChip(
        selected = seleccionado,
        onClick = alPulsar,
        label = etiqueta,
        modifier = modifier,
        enabled = habilitado,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = esquema.surfaceContainer,
            labelColor = esquema.onSurfaceVariant,
            selectedContainerColor = esquema.primary,
            selectedLabelColor = esquema.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = habilitado,
            selected = seleccionado,
            borderColor = esquema.outlineVariant,
            selectedBorderColor = esquema.primary,
        ),
    )
}
