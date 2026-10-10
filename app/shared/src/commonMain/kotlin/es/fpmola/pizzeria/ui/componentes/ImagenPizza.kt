package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

/**
 * Imagen de una pizza, cargada desde su URL absoluta con Coil (el backend no
 * sirve imágenes). Mientras carga, o si la URL falta o falla, muestra un
 * marcador de posición.
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
    if (url.isNullOrBlank()) {
        MarcadorImagen(modifier)
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = descripcion,
            modifier = modifier,
            contentScale = contentScale,
            loading = { MarcadorImagen(Modifier.fillMaxSize(), cargando = true) },
            error = { MarcadorImagen(Modifier.fillMaxSize()) },
        )
    }
}

/**
 * Recuadro de relleno: una rueda de carga o una pizza dibujada en emoji.
 */
@Composable
internal fun MarcadorImagen(
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(text = "🍕", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
