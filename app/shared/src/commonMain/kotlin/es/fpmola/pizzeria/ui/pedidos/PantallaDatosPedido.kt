package es.fpmola.pizzeria.ui.pedidos

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.pedidos.TipoPedido

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
            TextButton(onClick = alVolver, enabled = editable) {
                Text("‹ Carrito")
            }
            Text(
                text = "Datos del pedido",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
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
            Text("¿Cómo quieres tu pedido?", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (tipo in TipoPedido.entries) {
                    FilterChip(
                        selected = datos.tipo == tipo,
                        onClick = { alCambiarTipo(tipo) },
                        enabled = editable,
                        label = { Text(tipo.etiqueta) },
                    )
                }
            }

            // Solo se muestran los campos que exige el tipo elegido.
            when (datos.tipo) {
                TipoPedido.Mesa -> {
                    CampoTexto(
                        valor = datos.mesa,
                        alCambiar = alCambiarMesa,
                        etiqueta = "Número de mesa",
                        error = errores.mesa,
                        editable = editable,
                        teclado = KeyboardType.Number,
                    )
                    CampoTexto(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre (opcional)",
                        error = errores.nombre,
                        editable = editable,
                    )
                }
                TipoPedido.Recoger -> {
                    CampoTexto(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre",
                        error = errores.nombre,
                        editable = editable,
                    )
                    CampoTexto(
                        valor = datos.telefono,
                        alCambiar = alCambiarTelefono,
                        etiqueta = "Teléfono",
                        error = errores.telefono,
                        editable = editable,
                        teclado = KeyboardType.Phone,
                    )
                }
                TipoPedido.Domicilio -> {
                    CampoTexto(
                        valor = datos.nombre,
                        alCambiar = alCambiarNombre,
                        etiqueta = "Nombre",
                        error = errores.nombre,
                        editable = editable,
                    )
                    CampoTexto(
                        valor = datos.telefono,
                        alCambiar = alCambiarTelefono,
                        etiqueta = "Teléfono",
                        error = errores.telefono,
                        editable = editable,
                        teclado = KeyboardType.Phone,
                    )
                    CampoTexto(
                        valor = datos.direccion,
                        alCambiar = alCambiarDireccion,
                        etiqueta = "Dirección de entrega",
                        error = errores.direccion,
                        editable = editable,
                        unaLinea = false,
                    )
                }
            }

            CampoTexto(
                valor = datos.observaciones,
                alCambiar = alCambiarObservaciones,
                etiqueta = "Observaciones (opcional)",
                error = null,
                editable = editable,
                unaLinea = false,
            )
        }

        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = datos.tipo.descripcionPago + " El pago con tarjeta llegará más adelante.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "$unidades ${if (unidades == 1) "unidad" else "unidades"} · " +
                    "Total estimado ${total.formatear()}",
                style = MaterialTheme.typography.titleMedium,
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
                    Text(if (estado.envioIncierto) "Volver a enviar el pedido" else "Enviar pedido")
                }
            }
        }
    }
}

/** Campo de texto con el error de validación debajo, si lo hay. */
@Composable
private fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    editable: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        singleLine = unaLinea,
        enabled = editable,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth(),
    )
}
