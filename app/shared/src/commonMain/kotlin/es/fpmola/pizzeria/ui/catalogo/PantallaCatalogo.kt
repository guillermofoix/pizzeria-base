package es.fpmola.pizzeria.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.ui.componentes.BotonCarrito
import es.fpmola.pizzeria.ui.componentes.BotonTexto
import es.fpmola.pizzeria.ui.componentes.ChipPizzeria
import es.fpmola.pizzeria.ui.componentes.ImagenPizza
import es.fpmola.pizzeria.ui.componentes.TarjetaPizzeria

/**
 * Pantalla del catálogo: selector de categorías y lista de pizzas.
 *
 * Muestra carga, error con "Reintentar" y lista vacía. Las pizzas agotadas se
 * ven atenuadas y marcadas.
 */
@Composable
fun PantallaCatalogo(
    estado: EstadoCatalogo,
    alSeleccionarCategoria: (Int?) -> Unit,
    alReintentar: () -> Unit,
    alAbrirPizza: (Pizza) -> Unit,
    unidadesCarrito: Int,
    alAbrirCarrito: () -> Unit,
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
            BotonTexto(onClick = alVolver) {
                Text("‹ Inicio")
            }
            Text(
                text = "Nuestra carta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.weight(1f))
            BotonCarrito(unidades = unidadesCarrito, alAbrir = alAbrirCarrito)
        }

        when {
            estado.error != null -> VistaError(
                mensaje = estado.error,
                alReintentar = alReintentar,
                modifier = Modifier.weight(1f),
            )
            estado.enCarga -> VistaCargando(modifier = Modifier.weight(1f))
            else -> {
                SelectorCategorias(
                    estado = estado,
                    alSeleccionarCategoria = alSeleccionarCategoria,
                )
                ListaPizzas(
                    estado = estado,
                    alAbrirPizza = alAbrirPizza,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun VistaCargando(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando la carta…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VistaError(
    mensaje: String,
    alReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Button(onClick = alReintentar) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun SelectorCategorias(
    estado: EstadoCatalogo,
    alSeleccionarCategoria: (Int?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "todas") {
            ChipPizzeria(
                seleccionado = estado.categoriaSeleccionadaId == null,
                alPulsar = { alSeleccionarCategoria(null) },
                etiqueta = { Text("🔥 Todas") },
            )
        }
        items(estado.categorias, key = { it.id }) { categoria ->
            ChipPizzeria(
                seleccionado = estado.categoriaSeleccionadaId == categoria.id,
                alPulsar = { alSeleccionarCategoria(categoria.id) },
                etiqueta = {
                    Text(listOfNotNull(categoria.icono, categoria.nombre).joinToString(" "))
                },
            )
        }
    }
}

@Composable
private fun ListaPizzas(
    estado: EstadoCatalogo,
    alAbrirPizza: (Pizza) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pizzas = estado.pizzasFiltradas
    if (pizzas.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (estado.pizzas.isEmpty()) {
                    "La carta está vacía por ahora."
                } else {
                    "No hay pizzas en esta categoría."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(pizzas, key = { it.id }) { pizza ->
                TarjetaDePizza(pizza = pizza, alPulsar = { alAbrirPizza(pizza) })
            }
        }
    }
}

@Composable
private fun TarjetaDePizza(
    pizza: Pizza,
    alPulsar: () -> Unit,
) {
    val agotada = !pizza.disponible
    TarjetaPizzeria(
        alPulsar = alPulsar,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (agotada) 0.55f else 1f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ImagenPizza(
                url = pizza.imagenUrl,
                descripcion = pizza.nombre,
                modifier = Modifier
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pizza.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!pizza.descripcion.isNullOrBlank()) {
                    Text(
                        text = pizza.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = pizza.precio.formatear(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (agotada) {
                        Text(
                            text = "Agotada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
