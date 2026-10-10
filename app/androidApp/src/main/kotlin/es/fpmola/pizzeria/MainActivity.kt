package es.fpmola.pizzeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // El botón atrás del sistema se intercepta aquí, en Android, con
            // BackHandler de androidx.activity (estable); la navegación común
            // solo recibe "habilitado" y "qué hacer".
            App(
                manejadorAtras = { habilitado, alAtras ->
                    BackHandler(enabled = habilitado, onBack = alAtras)
                },
            )
        }
    }
}
