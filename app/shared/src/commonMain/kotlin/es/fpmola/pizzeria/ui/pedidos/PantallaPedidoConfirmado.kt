package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.pedidos.LineaPedido
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.TipoPedido

/**
 * Pantalla de pedido confirmado: número, tipo, líneas, total y estado. El
 * estado se actualiza solo mientras la pantalla está visible (lo lanza quien
 * la usa) y también con el botón "Actualizar".
 */
@Composable
fun PantallaPedidoConfirmado(
    estado: EstadoPedidoConfirmado,
    alActualizar: () -> Unit,
    alVolverAlInicio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pedido = estado.pedido

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "¡Pedido enviado!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            if (pedido == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Cargando tu pedido…")
                }
            } else {
                DetallePedido(pedido)
            }

            if (estado.error != null) {
                Text(
                    text = "No se ha podido actualizar el estado del pedido. ${estado.error}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = alActualizar,
                enabled = !estado.actualizando,
            ) {
                Text(if (estado.actualizando) "Actualizando…" else "Actualizar")
            }
            Button(
                onClick = alVolverAlInicio,
                modifier = Modifier.weight(1f),
            ) {
                Text("Volver al inicio")
            }
        }
    }
}

@Composable
private fun DetallePedido(pedido: Pedido) {
    val tipo = pedido.tipo
    val estado = pedido.estadoConocido

    Text(
        text = "Pedido n.º ${pedido.id}",
        style = MaterialTheme.typography.titleLarge,
    )

    // Tipo de pedido y, según el tipo, mesa o dirección.
    Text(
        text = buildString {
            append(tipo?.etiqueta ?: pedido.tipoPedido)
            if (tipo == TipoPedido.Mesa && pedido.mesaNumero != null) {
                append(" · Mesa ${pedido.mesaNumero}")
            }
        },
        style = MaterialTheme.typography.bodyLarge,
    )
    if (tipo == TipoPedido.Domicilio && !pedido.clienteDireccion.isNullOrBlank()) {
        Text(
            text = "Entrega en: ${pedido.clienteDireccion}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    // Estado actual del pedido.
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Estado", style = MaterialTheme.typography.labelMedium)
            Text(
                text = estado?.etiqueta ?: pedido.estado,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (pedido.esFinal) {
                    "El pedido ha terminado."
                } else {
                    "Se actualiza solo cada pocos segundos."
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    Text("Tu pedido", style = MaterialTheme.typography.titleMedium)
    for (linea in pedido.lineas) {
        FilaLinea(linea)
    }

    HorizontalDivider()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Total",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = pedido.total.formatear(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Text(
        text = "Pago: " + when (pedido.estadoPago) {
            "pagado" -> "pagado"
            "pendiente", null -> "pendiente (se paga en el local)"
            else -> pedido.estadoPago
        },
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun FilaLinea(linea: LineaPedido) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${linea.cantidad} × ${linea.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!linea.notas.isNullOrBlank()) {
                    Text(
                        text = linea.notas,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = linea.subtotalCalculado.formatear(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
