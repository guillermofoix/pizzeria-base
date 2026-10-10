package es.fpmola.pizzeria.ui.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.ui.componentes.BotonContorno
import es.fpmola.pizzeria.ui.componentes.CampoTextoPizzeria

/**
 * Pantalla para indicar la dirección del servidor, probar la conexión y
 * guardarla. Solo se puede guardar una dirección que haya superado la prueba.
 */
@Composable
fun PantallaConfiguracion(
    estado: EstadoConfiguracion,
    alCambiarUrl: (String) -> Unit,
    alProbarConexion: () -> Unit,
    alGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Configuración del servidor",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Escribe la dirección del servidor de la pizzería. Debe empezar por https://",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CampoTextoPizzeria(
            valor = estado.url,
            alCambiar = alCambiarUrl,
            etiqueta = "Dirección del servidor",
            placeholder = "https://…",
            habilitado = !estado.probando,
            opcionesTeclado = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            accionesTeclado = KeyboardActions(onDone = { alProbarConexion() }),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BotonContorno(
                onClick = alProbarConexion,
                enabled = !estado.probando,
            ) {
                Text("Probar conexión")
            }
            if (estado.probando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        estado.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estado.exito) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }

        Button(
            onClick = alGuardar,
            enabled = estado.puedeGuardar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Guardar y continuar")
        }
    }
}
