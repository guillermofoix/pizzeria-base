package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Imagen de una pizza.
 *
 * Por ahora muestra solo un marcador de posición; la carga de la imagen desde
 * su URL se añade en un commit posterior.
 *
 * @param url URL absoluta de la imagen, o null si la pizza no tiene.
 * @param descripcion texto para accesibilidad.
 */
@Composable
fun ImagenPizza(
    url: String?,
    descripcion: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    MarcadorImagen(modifier)
}

/** Recuadro con una pizza dibujada en emoji, para cuando no hay imagen. */
@Composable
internal fun MarcadorImagen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "🍕", style = MaterialTheme.typography.headlineMedium)
    }
}
