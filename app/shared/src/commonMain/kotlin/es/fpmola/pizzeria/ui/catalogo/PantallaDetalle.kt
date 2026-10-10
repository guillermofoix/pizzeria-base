package es.fpmola.pizzeria.ui.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.carrito.ResultadoAnadir
import es.fpmola.pizzeria.catalogo.Pizza
import es.fpmola.pizzeria.ui.componentes.BotonCarrito
import es.fpmola.pizzeria.ui.componentes.BotonTexto
import es.fpmola.pizzeria.ui.componentes.ImagenPizza
import es.fpmola.pizzeria.ui.componentes.SelectorCantidad

/**
 * Detalle de una pizza: imagen grande, descripción, precio y, solo si existen,
 * ingredientes y alérgenos. Permite elegir la cantidad y añadirla al carrito
 * (las pizzas agotadas no se pueden añadir).
 *
 * @param pizza la pizza a mostrar; null si todavía no está disponible.
 * @param cargando true mientras se carga la carta (la pizza puede llegar después).
 * @param unidadesCarrito unidades que hay en el carrito (para el indicador).
 * @param alAnadirAlCarrito añade las unidades elegidas y devuelve el resultado.
 */
@Composable
fun PantallaDetalle(
    pizza: Pizza?,
    cargando: Boolean,
    unidadesCarrito: Int,
    alAnadirAlCarrito: (Int) -> ResultadoAnadir,
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
                Text("‹ Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            BotonCarrito(unidades = unidadesCarrito, alAbrir = alAbrirCarrito)
        }

        when {
            pizza != null -> {
                ContenidoDetalle(pizza = pizza, modifier = Modifier.weight(1f))
                BarraAnadirAlCarrito(pizza = pizza, alAnadir = alAnadirAlCarrito)
            }
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

/**
 * Selector de cantidad y botón "Añadir al carrito", fijos en la parte
 * inferior. Con la pizza agotada el botón queda desactivado.
 */
@Composable
private fun BarraAnadirAlCarrito(
    pizza: Pizza,
    alAnadir: (Int) -> ResultadoAnadir,
) {
    var cantidad by remember(pizza.id) { mutableStateOf(Carrito.CANTIDAD_MINIMA) }
    var aviso by remember(pizza.id) { mutableStateOf<String?>(null) }

    HorizontalDivider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val mensaje = if (!pizza.disponible) {
            "Esta pizza está agotada y no se puede añadir al carrito."
        } else {
            aviso
        }
        if (mensaje != null) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = if (pizza.disponible) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SelectorCantidad(
                cantidad = cantidad,
                alCambiar = {
                    cantidad = it
                    aviso = null
                },
                habilitado = pizza.disponible,
            )
            Button(
                onClick = {
                    aviso = when (alAnadir(cantidad)) {
                        ResultadoAnadir.Anadida -> "Añadido al carrito."
                        ResultadoAnadir.LimiteAlcanzado ->
                            "Máximo ${Carrito.CANTIDAD_MAXIMA} unidades por pizza: se han añadido las que cabían."
                        ResultadoAnadir.NoDisponible -> "Esta pizza está agotada."
                    }
                    cantidad = Carrito.CANTIDAD_MINIMA
                },
                enabled = pizza.disponible,
                modifier = Modifier.weight(1f),
            ) {
                Text("Añadir al carrito")
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
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (!pizza.categoriaNombre.isNullOrBlank()) {
                Text(
                    text = listOfNotNull(pizza.categoriaIcono, pizza.categoriaNombre).joinToString(" "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pizza.precio.formatear(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    texto = "⚠️ Contiene ingredientes alérgenos: " +
                        alergenos.joinToString(", ") { it.nombre },
                    colorTexto = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun SeccionTexto(
    titulo: String,
    texto: String,
    colorTexto: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = colorTexto,
        )
    }
}
