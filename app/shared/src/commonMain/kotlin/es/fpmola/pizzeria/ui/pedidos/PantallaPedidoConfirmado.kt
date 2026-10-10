package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.pedidos.LineaPedido
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.ui.componentes.BarraProgresoPedido
import es.fpmola.pizzeria.ui.componentes.BotonContorno
import es.fpmola.pizzeria.ui.componentes.InsigniaEstado
import es.fpmola.pizzeria.ui.componentes.TarjetaPizzeria
import es.fpmola.pizzeria.ui.componentes.emoji
import es.fpmola.pizzeria.ui.componentes.presentacionEstado

/**
 * Pantalla de pedido confirmado: número, tipo, líneas, total y estado. El
 * estado se actualiza solo mientras la pantalla está visible (lo lanza quien
 * la usa) y también con el botón "Actualizar".
 */
@Composable
fun PantallaPedidoConfirmado(
    estado: EstadoPedidoConfirmado,
    alActualizar: () -> Unit,
    alPagarConTarjeta: () -> Unit,
    alComprobarPago: () -> Unit,
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
                text = "✅ ¡Pedido enviado!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (pedido == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Cargando tu pedido…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                DetallePedido(pedido)
                BloquePago(pedido, estado, alPagarConTarjeta, alComprobarPago)
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
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BotonContorno(
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
    val presentacion = presentacionEstado(estado)

    Text(
        text = "Pedido n.º ${pedido.id}",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )

    // Tipo de pedido y, según el tipo, mesa o dirección.
    Text(
        text = buildString {
            if (tipo != null) append("${tipo.emoji} ")
            append(tipo?.etiqueta ?: pedido.tipoPedido)
            if (tipo == TipoPedido.Mesa && pedido.mesaNumero != null) {
                append(" · Mesa ${pedido.mesaNumero}")
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    if (tipo == TipoPedido.Domicilio && !pedido.clienteDireccion.isNullOrBlank()) {
        Text(
            text = "Entrega en: ${pedido.clienteDireccion}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    // Estado actual del pedido: insignia y barra de progreso, como la web.
    TarjetaPizzeria(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Estado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InsigniaEstado(
                texto = estado?.etiqueta ?: pedido.estado,
                presentacion = presentacion,
            )
            if (presentacion.avance != null) {
                BarraProgresoPedido(avance = presentacion.avance, color = presentacion.color)
            }
            Text(
                text = if (pedido.esFinal) {
                    "El pedido ha terminado."
                } else {
                    "Se actualiza solo cada pocos segundos."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Text(
        text = "Tu pedido",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = pedido.total.formatear(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    // Con tarjeta, el pago se muestra en su propio bloque (ver BloquePago).
    if (!pedido.pagoConTarjeta) {
        Text(
            text = "Pago: " + when (pedido.estadoPago) {
                "pagado" -> "pagado"
                "pendiente", null -> "pendiente (se paga en el local)"
                else -> pedido.estadoPago
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Pago con tarjeta: "Pago recibido" cuando el servidor lo ha confirmado o
 * "Pago pendiente" con los botones para pagar y para comprobar. No se muestra
 * en pedidos que no son con tarjeta.
 */
@Composable
private fun BloquePago(
    pedido: Pedido,
    estado: EstadoPedidoConfirmado,
    alPagarConTarjeta: () -> Unit,
    alComprobarPago: () -> Unit,
) {
    if (!pedido.pagoConTarjeta) return

    TarjetaPizzeria(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Pago",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                pedido.pagado -> Text(
                    text = "✅ Pago recibido",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                !pedido.pagoConTarjetaPendiente -> Text(
                    text = "El pedido está cancelado: no se cobrará.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> PagoPendiente(estado, alPagarConTarjeta, alComprobarPago)
            }
        }
    }
}

@Composable
private fun PagoPendiente(
    estado: EstadoPedidoConfirmado,
    alPagarConTarjeta: () -> Unit,
    alComprobarPago: () -> Unit,
) {
    Text(
        text = "💳 Pago pendiente",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = "Cuando termines de pagar, Stripe abrirá la web de la pizzería. " +
            "Vuelve a esta app: se actualizará sola.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (estado.errorPago != null) {
        Text(
            text = estado.errorPago,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (estado.infoPago != null) {
        Text(
            text = estado.infoPago,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Button(
        onClick = alPagarConTarjeta,
        enabled = !estado.procesandoPago,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (estado.errorPago != null) "Reintentar pago" else "Pagar con tarjeta")
    }
    BotonContorno(
        onClick = alComprobarPago,
        enabled = !estado.procesandoPago,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Ya he pagado, comprobar")
    }
}

@Composable
private fun FilaLinea(linea: LineaPedido) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${linea.cantidad} × ${linea.nombre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!linea.notas.isNullOrBlank()) {
                Text(
                    text = "⚠️ ${linea.notas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Text(
            text = linea.subtotalCalculado.formatear(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
