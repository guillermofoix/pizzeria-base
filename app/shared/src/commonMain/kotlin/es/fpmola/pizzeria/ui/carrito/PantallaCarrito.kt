package es.fpmola.pizzeria.ui.carrito

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.carrito.LineaCarrito
import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.ui.componentes.BotonContorno
import es.fpmola.pizzeria.ui.componentes.BotonTexto
import es.fpmola.pizzeria.ui.componentes.ImagenPizza
import es.fpmola.pizzeria.ui.componentes.SelectorCantidad
import es.fpmola.pizzeria.ui.componentes.TarjetaPizzeria

/**
 * Pantalla del carrito: líneas con imagen, nombre, cantidad editable y
 * subtotal; total; y botones para vaciar y continuar. Si está vacío ofrece
 * volver a la carta.
 *
 * @param avisoQuitadas nombres de pizzas que se quitaron por no estar ya en la
 * carta o estar agotadas (vacío si no hay aviso).
 */
@Composable
fun PantallaCarrito(
    lineas: List<LineaCarrito>,
    total: Importe,
    avisoQuitadas: List<String>,
    alCambiarCantidad: (pizzaId: Int, cantidad: Int) -> Unit,
    alQuitar: (pizzaId: Int) -> Unit,
    alVaciar: () -> Unit,
    alDescartarAviso: () -> Unit,
    alContinuar: () -> Unit,
    alVolverALaCarta: () -> Unit,
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
            Text(
                text = "🛒 Tu carrito",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (avisoQuitadas.isNotEmpty()) {
            AvisoQuitadas(nombres = avisoQuitadas, alDescartar = alDescartarAviso)
        }

        if (lineas.isEmpty()) {
            CarritoVacio(
                alVolverALaCarta = alVolverALaCarta,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lineas, key = { it.pizzaId }) { linea ->
                    FilaCarrito(
                        linea = linea,
                        alCambiarCantidad = { alCambiarCantidad(linea.pizzaId, it) },
                        alQuitar = { alQuitar(linea.pizzaId) },
                    )
                }
            }
            ResumenCarrito(
                total = total,
                alVaciar = alVaciar,
                alContinuar = alContinuar,
            )
        }
    }
}

@Composable
private fun AvisoQuitadas(
    nombres: List<String>,
    alDescartar: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Hemos quitado del carrito lo que ya no está disponible en la carta: " +
                    nombres.joinToString(", ") + ".",
                style = MaterialTheme.typography.bodyMedium,
            )
            BotonTexto(onClick = alDescartar) {
                Text("Entendido")
            }
        }
    }
}

@Composable
private fun CarritoVacio(
    alVolverALaCarta: () -> Unit,
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
                text = "Tu carrito está vacío.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = alVolverALaCarta) {
                Text("Ver la carta")
            }
        }
    }
}

@Composable
private fun FilaCarrito(
    linea: LineaCarrito,
    alCambiarCantidad: (Int) -> Unit,
    alQuitar: () -> Unit,
) {
    TarjetaPizzeria(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImagenPizza(
                    url = linea.imagenUrl,
                    descripcion = linea.nombre,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = linea.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${linea.precioUnitario.formatear()} por unidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SelectorCantidad(
                    cantidad = linea.cantidad,
                    alCambiar = alCambiarCantidad,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = linea.subtotal.formatear(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                BotonTexto(onClick = alQuitar) {
                    Text("Quitar")
                }
            }
        }
    }
}

@Composable
private fun ResumenCarrito(
    total: Importe,
    alVaciar: () -> Unit,
    alContinuar: () -> Unit,
) {
    HorizontalDivider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total estimado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = total.formatear(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = "El importe definitivo lo calcula el restaurante al recibir el pedido.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BotonContorno(onClick = alVaciar) {
                Text("Vaciar carrito")
            }
            Button(
                onClick = alContinuar,
                modifier = Modifier.weight(1f),
            ) {
                Text("Continuar")
            }
        }
    }
}
