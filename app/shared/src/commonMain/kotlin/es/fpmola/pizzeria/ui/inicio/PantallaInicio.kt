package es.fpmola.pizzeria.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla de inicio (vacía por ahora). Se llega tras una conexión correcta.
 */
@Composable
fun PantallaInicio(
    urlServidor: String?,
    alCambiarServidor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        if (urlServidor != null) {
            Text(
                text = "Conectado a $urlServidor",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        TextButton(onClick = alCambiarServidor) {
            Text("Cambiar servidor")
        }
    }
}
