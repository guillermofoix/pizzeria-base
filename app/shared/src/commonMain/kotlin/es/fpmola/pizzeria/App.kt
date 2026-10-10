package es.fpmola.pizzeria

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fpmola.pizzeria.di.Dependencias
import es.fpmola.pizzeria.ui.AvisoDemo
import es.fpmola.pizzeria.ui.catalogo.CatalogoViewModel
import es.fpmola.pizzeria.ui.catalogo.PantallaCatalogo
import es.fpmola.pizzeria.ui.catalogo.PantallaDetalle
import es.fpmola.pizzeria.ui.configuracion.ConfiguracionViewModel
import es.fpmola.pizzeria.ui.configuracion.PantallaConfiguracion
import es.fpmola.pizzeria.ui.inicio.PantallaInicio
import es.fpmola.pizzeria.ui.tema.TemaPizzeria

/** Pantallas de la app. La navegación es un simple estado, sin librería. */
private enum class Pantalla { Configuracion, Inicio, Catalogo, Detalle }

/**
 * Raíz de la app: tema de la pizzería, aviso de demo siempre visible y
 * navegación entre configuración del servidor, inicio, catálogo y detalle.
 *
 * @param manejadorAtras permite a cada plataforma interceptar el botón atrás
 * del sistema. Recibe si debe estar activo y qué hacer al pulsarlo. En Android
 * se implementa con BackHandler (ver MainActivity).
 */
@Composable
fun App(
    manejadorAtras: @Composable (habilitado: Boolean, alAtras: () -> Unit) -> Unit = { _, _ -> },
) {
    TemaPizzeria {
        var pantalla by rememberSaveable { mutableStateOf(Pantalla.Configuracion) }
        var pizzaSeleccionadaId by rememberSaveable { mutableStateOf<Int?>(null) }
        // Cambian al guardar otro servidor o al pulsar "Reintentar": el catálogo
        // de un servidor anterior no se reutiliza.
        var versionServidor by rememberSaveable { mutableStateOf(0) }
        var intentoCatalogo by rememberSaveable { mutableStateOf(0) }

        // Atrás: del detalle al catálogo y del catálogo al inicio.
        manejadorAtras(pantalla == Pantalla.Detalle || pantalla == Pantalla.Catalogo) {
            pantalla = if (pantalla == Pantalla.Detalle) Pantalla.Catalogo else Pantalla.Inicio
        }

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
                            if (modelo.guardarUrl()) {
                                versionServidor++
                                pantalla = Pantalla.Inicio
                            }
                        },
                        modifier = modificador,
                    )
                }
                Pantalla.Inicio -> {
                    val urlServidor = remember { Dependencias.ajustes().obtenerUrlBase() }
                    PantallaInicio(
                        urlServidor = urlServidor,
                        alVerCarta = { pantalla = Pantalla.Catalogo },
                        alCambiarServidor = { pantalla = Pantalla.Configuracion },
                        modifier = modificador,
                    )
                }
                Pantalla.Catalogo -> {
                    val modelo = catalogoViewModel(versionServidor, intentoCatalogo)
                    PantallaCatalogo(
                        estado = modelo.estado,
                        alSeleccionarCategoria = modelo::seleccionarCategoria,
                        alReintentar = { intentoCatalogo++ },
                        alAbrirPizza = { pizza ->
                            pizzaSeleccionadaId = pizza.id
                            pantalla = Pantalla.Detalle
                        },
                        alVolver = { pantalla = Pantalla.Inicio },
                        modifier = modificador,
                    )
                }
                Pantalla.Detalle -> {
                    val modelo = catalogoViewModel(versionServidor, intentoCatalogo)
                    PantallaDetalle(
                        pizza = modelo.estado.pizzas.firstOrNull { it.id == pizzaSeleccionadaId },
                        cargando = modelo.estado.enCarga,
                        alVolver = { pantalla = Pantalla.Catalogo },
                        modifier = modificador,
                    )
                }
            }
        }
    }
}

/**
 * Obtiene el [CatalogoViewModel] compartido por el catálogo y el detalle (así
 * el detalle ve las mismas pizzas y volver atrás no recarga la carta) y lanza
 * su carga si hace falta. Un [versionServidor] distinto crea un modelo nuevo;
 * un [intento] distinto repite la carga tras un error.
 */
@Composable
private fun catalogoViewModel(versionServidor: Int, intento: Int): CatalogoViewModel {
    val modelo = viewModel<CatalogoViewModel>(key = "catalogo-$versionServidor") {
        CatalogoViewModel(Dependencias.repositorioCatalogo())
    }
    LaunchedEffect(modelo, intento) {
        modelo.cargarSiHaceFalta()
    }
    return modelo
}
