package es.fpmola.pizzeria.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.ui.componentes.BotonTexto

/**
 * Pantalla de inicio. Se llega tras una conexión correcta y desde aquí se
 * abre la carta. Lleva la marca como la cabecera de la web de clientes.
 */
@Composable
fun PantallaInicio(
    urlServidor: String?,
    alVerCarta: () -> Unit,
    alCambiarServidor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "🍕", style = MaterialTheme.typography.displaySmall)
            Column {
                Text(
                    text = "Pizzería Bella Napoli",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Pizzería artesanal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (urlServidor != null) {
            Text(
                text = "Conectado a $urlServidor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = alVerCarta,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("🍕 Ver la carta")
        }
        BotonTexto(onClick = alCambiarServidor) {
            Text("Cambiar servidor")
        }
    }
}
