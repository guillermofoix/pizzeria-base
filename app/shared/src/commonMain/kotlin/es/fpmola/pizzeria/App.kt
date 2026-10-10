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
import es.fpmola.pizzeria.carrito.ResultadoAnadir
import es.fpmola.pizzeria.di.Dependencias
import es.fpmola.pizzeria.ui.AvisoDemo
import es.fpmola.pizzeria.ui.catalogo.CatalogoViewModel
import es.fpmola.pizzeria.ui.catalogo.PantallaCatalogo
import es.fpmola.pizzeria.ui.carrito.PantallaCarrito
import es.fpmola.pizzeria.ui.catalogo.PantallaDetalle
import es.fpmola.pizzeria.ui.configuracion.ConfiguracionViewModel
import es.fpmola.pizzeria.ui.configuracion.PantallaConfiguracion
import es.fpmola.pizzeria.ui.inicio.PantallaInicio
import es.fpmola.pizzeria.ui.tema.TemaPizzeria

/** Pantallas de la app. La navegación es un simple estado, sin librería. */
private enum class Pantalla { Configuracion, Inicio, Catalogo, Detalle, Carrito }

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
        // Pantalla desde la que se abrió el carrito (catálogo o detalle), para volver a ella.
        var origenCarrito by rememberSaveable { mutableStateOf(Pantalla.Catalogo) }
        val carrito = remember { Dependencias.carrito() }

        // Atrás: del detalle al catálogo, del catálogo al inicio y del carrito
        // a la pantalla desde la que se abrió.
        manejadorAtras(
            pantalla == Pantalla.Detalle || pantalla == Pantalla.Catalogo || pantalla == Pantalla.Carrito,
        ) {
            pantalla = when (pantalla) {
                Pantalla.Detalle -> Pantalla.Catalogo
                Pantalla.Carrito -> origenCarrito
                else -> Pantalla.Inicio
            }
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
                        unidadesCarrito = carrito.totalUnidades,
                        alAbrirCarrito = {
                            origenCarrito = Pantalla.Catalogo
                            pantalla = Pantalla.Carrito
                        },
                        alVolver = { pantalla = Pantalla.Inicio },
                        modifier = modificador,
                    )
                }
                Pantalla.Detalle -> {
                    val modelo = catalogoViewModel(versionServidor, intentoCatalogo)
                    val pizza = modelo.estado.pizzas.firstOrNull { it.id == pizzaSeleccionadaId }
                    PantallaDetalle(
                        pizza = pizza,
                        cargando = modelo.estado.enCarga,
                        unidadesCarrito = carrito.totalUnidades,
                        alAnadirAlCarrito = { cantidad ->
                            if (pizza != null) {
                                carrito.anadir(pizza, cantidad)
                            } else {
                                ResultadoAnadir.NoDisponible
                            }
                        },
                        alAbrirCarrito = {
                            origenCarrito = Pantalla.Detalle
                            pantalla = Pantalla.Carrito
                        },
                        alVolver = { pantalla = Pantalla.Catalogo },
                        modifier = modificador,
                    )
                }
                Pantalla.Carrito -> {
                    // Se obtiene el modelo del catálogo para que la carta esté
                    // cargada y el carrito se ajuste a ella.
                    catalogoViewModel(versionServidor, intentoCatalogo)
                    PantallaCarrito(
                        lineas = carrito.lineas,
                        total = carrito.total,
                        avisoQuitadas = carrito.quitadasPorCarta,
                        alCambiarCantidad = carrito::cambiarCantidad,
                        alQuitar = carrito::quitar,
                        alVaciar = carrito::vaciar,
                        alDescartarAviso = carrito::descartarAviso,
                        // La pantalla de datos del pedido se conecta en un commit posterior.
                        alContinuar = { },
                        alVolverALaCarta = { pantalla = Pantalla.Catalogo },
                        alVolver = { pantalla = origenCarrito },
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
    // Cada vez que llega una carta nueva, el carrito se ajusta a ella: así no
    // se envían ids de pizzas que ya no existen o están agotadas.
    val carrito = remember { Dependencias.carrito() }
    LaunchedEffect(modelo.estado.pizzas) {
        if (modelo.estado.cargada) carrito.sincronizarConCarta(modelo.estado.pizzas)
    }
    return modelo
}
