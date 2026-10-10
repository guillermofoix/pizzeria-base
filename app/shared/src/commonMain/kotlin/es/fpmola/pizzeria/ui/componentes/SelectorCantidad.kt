package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.carrito.Carrito

/**
 * Selector de cantidad con botones "−" y "+", limitado entre [minimo] y [maximo].
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { alCambiar(cantidad - 1) },
            enabled = habilitado && cantidad > minimo,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(40.dp)
                .semantics { contentDescription = "Quitar una unidad" },
        ) {
            Text("−")
        }
        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp),
        )
        OutlinedButton(
            onClick = { alCambiar(cantidad + 1) },
            enabled = habilitado && cantidad < maximo,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(40.dp)
                .semantics { contentDescription = "Añadir una unidad" },
        ) {
            Text("+")
        }
    }
}
