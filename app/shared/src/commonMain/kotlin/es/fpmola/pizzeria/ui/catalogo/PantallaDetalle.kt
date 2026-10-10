package es.fpmola.pizzeria.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.ui.componentes.ImagenPizza

/**
 * Detalle de una pizza: imagen grande, descripción, precio y, solo si existen,
 * ingredientes y alérgenos.
 *
 * @param pizza la pizza a mostrar; null si todavía no está disponible.
 * @param cargando true mientras se carga la carta (la pizza puede llegar después).
 */
@Composable
fun PantallaDetalle(
    pizza: Pizza?,
    cargando: Boolean,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = alVolver) {
                Text("‹ Volver")
            }
        }

        when {
            pizza != null -> ContenidoDetalle(pizza = pizza, modifier = Modifier.weight(1f))
            cargando -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            else -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No se ha encontrado esta pizza en la carta.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    pizza: Pizza,
    modifier: Modifier = Modifier,
) {
    val agotada = !pizza.disponible
    val alergenos = pizza.ingredientesAlergenos

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        ImagenPizza(
            url = pizza.imagenUrl,
            descripcion = pizza.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .alpha(if (agotada) 0.55f else 1f),
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = pizza.nombre,
                style = MaterialTheme.typography.headlineSmall,
            )

            if (!pizza.categoriaNombre.isNullOrBlank()) {
                Text(
                    text = listOfNotNull(pizza.categoriaIcono, pizza.categoriaNombre).joinToString(" "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pizza.precio.formatear(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (agotada) {
                    Text(
                        text = "Agotada",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (!pizza.descripcion.isNullOrBlank()) {
                Text(
                    text = pizza.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            // Solo se muestra la sección si la pizza tiene ingredientes.
            if (pizza.ingredientes.isNotEmpty()) {
                SeccionTexto(
                    titulo = "Ingredientes",
                    texto = pizza.ingredientes.joinToString(", ") { it.nombre },
                )
            }

            // Solo se muestra la sección si algún ingrediente es alérgeno.
            if (alergenos.isNotEmpty()) {
                SeccionTexto(
                    titulo = "Alérgenos",
                    texto = "Contiene ingredientes alérgenos: " +
                        alergenos.joinToString(", ") { it.nombre },
                    colorTexto = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SeccionTexto(
    titulo: String,
    texto: String,
    colorTexto: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = colorTexto,
        )
    }
}
