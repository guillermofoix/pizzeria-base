package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.carrito.Carrito

/**
 * Selector de cantidad como el de la web: una píldora con dos botones
 * redondos "−" y "+", limitado entre [minimo] y [maximo].
 */
@Composable
fun SelectorCantidad(
    cantidad: Int,
    alCambiar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minimo: Int = Carrito.CANTIDAD_MINIMA,
    maximo: Int = Carrito.CANTIDAD_MAXIMA,
    habilitado: Boolean = true,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotonRedondo(
                texto = "−",
                descripcion = "Quitar una unidad",
                habilitado = habilitado && cantidad > minimo,
                alPulsar = { alCambiar(cantidad - 1) },
            )
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            BotonRedondo(
                texto = "+",
                descripcion = "Añadir una unidad",
                habilitado = habilitado && cantidad < maximo,
                alPulsar = { alCambiar(cantidad + 1) },
            )
        }
    }
}

@Composable
private fun BotonRedondo(
    texto: String,
    descripcion: String,
    habilitado: Boolean,
    alPulsar: () -> Unit,
) {
    val esquema = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = esquema.surfaceContainer,
            contentColor = esquema.onSurface,
        ),
        border = BorderStroke(1.dp, esquema.outlineVariant),
        modifier = Modifier
            .size(32.dp)
            .semantics { contentDescription = descripcion },
    ) {
        Text(texto)
    }
}
