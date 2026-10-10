package es.fpmola.pizzeria.ui.componentes

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Indicador del carrito para las barras de las pantallas: muestra el número
 * de unidades y abre el carrito al pulsarlo.
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
    TextButton(
        onClick = alAbrir,
        modifier = modifier.semantics { contentDescription = descripcion },
    ) {
        Text("🛒 $unidades")
    }
}
