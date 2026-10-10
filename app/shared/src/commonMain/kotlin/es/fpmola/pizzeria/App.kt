package es.fpmola.pizzeria

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fpmola.pizzeria.di.Dependencias
import es.fpmola.pizzeria.ui.AvisoDemo
import es.fpmola.pizzeria.ui.configuracion.ConfiguracionViewModel
import es.fpmola.pizzeria.ui.configuracion.PantallaConfiguracion
import es.fpmola.pizzeria.ui.inicio.PantallaInicio
import es.fpmola.pizzeria.ui.tema.TemaPizzeria

/** Pantallas de la app. La navegación es un simple estado, sin librería. */
private enum class Pantalla { Configuracion, Inicio }

/**
 * Raíz de la app: tema de la pizzería, aviso de demo siempre visible y
 * navegación entre configuración del servidor e inicio.
 */
@Composable
fun App() {
    TemaPizzeria {
        var pantalla by rememberSaveable { mutableStateOf(Pantalla.Configuracion) }

        Scaffold(bottomBar = { AvisoDemo() }) { relleno ->
            val modificador = Modifier
                .padding(relleno)
                .fillMaxSize()

            when (pantalla) {
                Pantalla.Configuracion -> {
                    val modelo = viewModel {
                        ConfiguracionViewModel(
                            ajustes = Dependencias.ajustes(),
                            repositorioSalud = Dependencias.repositorioSalud(),
                        )
                    }
                    PantallaConfiguracion(
                        estado = modelo.estado,
                        alCambiarUrl = modelo::cambiarUrl,
                        alProbarConexion = modelo::probarConexion,
                        alGuardar = {
                            if (modelo.guardarUrl()) pantalla = Pantalla.Inicio
                        },
                        modifier = modificador,
                    )
                }
                Pantalla.Inicio -> {
                    val urlServidor = remember { Dependencias.ajustes().obtenerUrlBase() }
                    PantallaInicio(
                        urlServidor = urlServidor,
                        alCambiarServidor = { pantalla = Pantalla.Configuracion },
                        modifier = modificador,
                    )
                }
            }
        }
    }
}
