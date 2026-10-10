package es.fpmola.pizzeria.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.pedidos.TipoPedido
import es.fpmola.pizzeria.ui.componentes.BotonTexto
import es.fpmola.pizzeria.ui.componentes.CampoTextoPizzeria
import es.fpmola.pizzeria.ui.componentes.ChipPizzeria
import es.fpmola.pizzeria.ui.componentes.emoji

/**
 * Pantalla de datos del pedido: tipo (mesa, recoger o domicilio) y solo los
 * campos que exige cada tipo, con los errores de validación en español.
 *
 * @param unidades unidades del carrito (para el resumen).
 * @param total total estimado del carrito (para el resumen).
 */
@Composable
fun PantallaDatosPedido(
    estado: EstadoDatosPedido,
    unidades: Int,
    total: Importe,
    alCambiarTipo: (TipoPedido) -> Unit,
    alCambiarMesa: (String) -> Unit,
    alCambiarNombre: (String) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alCambiarDireccion: (String) -> Unit,
    alCambiarObservaciones: (String) -> Unit,
    alEnviar: () -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datos = estado.datos
    val errores = estado.errores
    val editable = !estado.enviando

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotonTexto(onClick = alVolver, enabled = editable) {
                Text("‹ Carrito")
            }
            Text(
                text = "Datos del pedido",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "¿Cómo quieres tu pedido?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (tipo in TipoPedido.entries) {
                    ChipPizzeria(
                        seleccionado = datos.tipo == tipo,
                        alPulsar = { alCambiarTipo(tipo) },
                        habilitado = editable,
                        etiqueta = { Text("${tipo.emoji} ${tipo.etiqueta}") },
                    )
                }
            }

            // Solo se muestran los campos que exige el tipo elegido.
            when (datos.tipo) {
                TipoPedido.Mesa -> {
                    CampoTextoPizzeria(
                        valor = datos.mesa,
                        alCambiar = alCambiarMesa,
                        etiqueta = "Número de mesa",
                        error = errores.mesa,
                        habilitado = editable,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    CampoTextoPizzeria(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre (opcional)",
                        error = errores.nombre,
                        habilitado = editable,
                    )
                }
                TipoPedido.Recoger -> {
                    CampoTextoPizzeria(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre",
                        error = errores.nombre,
                        habilitado = editable,
                    )
                    CampoTextoPizzeria(
                        valor = datos.telefono,
                        alCambiar = alCambiarTelefono,
                        etiqueta = "Teléfono",
                        error = errores.telefono,
                        habilitado = editable,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
                TipoPedido.Domicilio -> {
                    CampoTextoPizzeria(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre",
                        error = errores.nombre,
                        habilitado = editable,
                    )
                    CampoTextoPizzeria(
                        valor = datos.telefono,
                        alCambiar = alCambiarTelefono,
                        etiqueta = "Teléfono",
                        error = errores.telefono,
                        habilitado = editable,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    CampoTextoPizzeria(
                        valor = datos.direccion,
                        alCambiar = alCambiarDireccion,
                        etiqueta = "Dirección de entrega",
                        error = errores.direccion,
                        habilitado = editable,
                        unaLinea = false,
                    )
                }
            }

            CampoTextoPizzeria(
                valor = datos.observaciones,
                alCambiar = alCambiarObservaciones,
                etiqueta = "Observaciones (opcional)",
                error = null,
                habilitado = editable,
                unaLinea = false,
            )
        }

        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = datos.tipo.descripcionPago + " El pago con tarjeta llegará más adelante.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$unidades ${if (unidades == 1) "unidad" else "unidades"} · " +
                    "Total estimado ${total.formatear()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (estado.mensajeError != null) {
                Text(
                    text = estado.mensajeError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = alEnviar,
                enabled = editable,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (estado.enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(text = "Enviando…", modifier = Modifier.padding(start = 12.dp))
                } else {
                    Text(if (estado.envioIncierto) "Volver a enviar el pedido" else "🚀 Enviar pedido")
                }
            }
        }
    }
}
