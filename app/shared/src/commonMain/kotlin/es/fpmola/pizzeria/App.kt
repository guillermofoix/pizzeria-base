package es.fpmola.pizzeria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.fpmola.pizzeria.ui.AvisoDemo
import es.fpmola.pizzeria.ui.tema.TemaPizzeria

/**
 * Raíz de la app: tema de la pizzería y aviso de demo siempre visible.
 */
@Composable
fun App() {
    TemaPizzeria {
        Scaffold(bottomBar = { AvisoDemo() }) { relleno ->
            Box(
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Pizzería Bella Napoli",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "App de clientes",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
