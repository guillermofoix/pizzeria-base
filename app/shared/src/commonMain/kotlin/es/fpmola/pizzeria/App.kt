package es.fpmola.pizzeria

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fpmola.pizzeria.carrito.ResultadoAnadir
import es.fpmola.pizzeria.di.Dependencias
import es.fpmola.pizzeria.pedidos.Pedido
import es.fpmola.pizzeria.ui.AvisoDemo
import es.fpmola.pizzeria.ui.carrito.PantallaCarrito
import es.fpmola.pizzeria.ui.catalogo.CatalogoViewModel
import es.fpmola.pizzeria.ui.catalogo.PantallaCatalogo
import es.fpmola.pizzeria.ui.catalogo.PantallaDetalle
import es.fpmola.pizzeria.ui.configuracion.ConfiguracionViewModel
import es.fpmola.pizzeria.ui.configuracion.PantallaConfiguracion
import es.fpmola.pizzeria.ui.inicio.PantallaInicio
import es.fpmola.pizzeria.ui.pedidos.DatosPedidoViewModel
import es.fpmola.pizzeria.ui.pedidos.PantallaDatosPedido
import es.fpmola.pizzeria.ui.pedidos.PantallaPedidoConfirmado
import es.fpmola.pizzeria.ui.pedidos.PedidoConfirmadoViewModel
import es.fpmola.pizzeria.ui.tema.TemaPizzeria
import kotlinx.coroutines.launch

/** Pantallas de la app. La navegación es un simple estado, sin librería. */
private enum class Pantalla { Configuracion, Inicio, Catalogo, Detalle, Carrito, Datos, Confirmado }

/**
 * Raíz de la app: tema de la pizzería, aviso de demo siempre visible y
 * navegación por estado: Inicio → Catálogo → Detalle → Carrito → Datos →
 * Confirmado.
 *
 * Atrás: Detalle → Catálogo; Catálogo → Inicio; Carrito → la pantalla desde la
 * que se abrió; Datos → Carrito (bloqueado mientras se envía el pedido);
 * Confirmado → Inicio (nunca vuelve al formulario).
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
        // Pedido recién creado. El contador cambia con cada pedido enviado para que
        // el siguiente empiece con un formulario nuevo.
        var pedidoConfirmadoId by rememberSaveable { mutableStateOf<Int?>(null) }
        var pedidosEnviados by rememberSaveable { mutableStateOf(0) }
        var pedidoConfirmado by remember { mutableStateOf<Pedido?>(null) }
        // true mientras se envía el pedido: el botón atrás no hace nada.
        var enviandoPedido by remember { mutableStateOf(false) }
        val carrito = remember { Dependencias.carrito() }

        manejadorAtras(
            pantalla != Pantalla.Configuracion && pantalla != Pantalla.Inicio,
        ) {
            pantalla = when (pantalla) {
                Pantalla.Detalle -> Pantalla.Catalogo
                Pantalla.Carrito -> origenCarrito
                // Mientras se envía el pedido, atrás no hace nada.
                Pantalla.Datos -> if (enviandoPedido) Pantalla.Datos else Pantalla.Carrito
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
                        alContinuar = {
                            // Si había un aviso de pizzas quitadas, ya se ha visto.
                            carrito.descartarAviso()
                            pantalla = Pantalla.Datos
                        },
                        alVolverALaCarta = { pantalla = Pantalla.Catalogo },
                        alVolver = { pantalla = origenCarrito },
                        modifier = modificador,
                    )
                }
                Pantalla.Datos -> {
                    val catalogo = catalogoViewModel(versionServidor, intentoCatalogo)
                    val modelo = viewModel<DatosPedidoViewModel>(
                        key = "datos-$versionServidor-$pedidosEnviados",
                    ) {
                        DatosPedidoViewModel(Dependencias.repositorioPedidos(), Dependencias.carrito())
                    }
                    val estadoDatos = modelo.estado

                    LaunchedEffect(estadoDatos.enviando) {
                        enviandoPedido = estadoDatos.enviando
                    }
                    // Pedido creado: pasar a la confirmación. La pantalla de datos
                    // no se puede volver a abrir con el pedido ya enviado.
                    LaunchedEffect(estadoDatos.pedidoCreado) {
                        val creado = estadoDatos.pedidoCreado
                        if (creado != null) {
                            pedidoConfirmado = creado
                            pedidoConfirmadoId = creado.id
                            pedidosEnviados++
                            pantalla = Pantalla.Confirmado
                        }
                    }
                    // Sin nada en el carrito no hay pedido que enviar.
                    LaunchedEffect(carrito.estaVacio, estadoDatos.pedidoCreado) {
                        if (carrito.estaVacio && estadoDatos.pedidoCreado == null) {
                            pantalla = Pantalla.Carrito
                        }
                    }
                    // Si se quitaron pizzas porque ya no están en la carta, el
                    // usuario debe verlo en el carrito antes de enviar.
                    LaunchedEffect(carrito.quitadasPorCarta) {
                        if (carrito.quitadasPorCarta.isNotEmpty()) pantalla = Pantalla.Carrito
                    }
                    // El servidor rechazó el pedido: se vuelve a cargar la carta y el
                    // carrito se ajusta a ella (ver catalogoViewModel).
                    LaunchedEffect(estadoDatos.cartaPorRefrescar) {
                        if (estadoDatos.cartaPorRefrescar) {
                            catalogo.cargar()
                            modelo.cartaRefrescada()
                        }
                    }

                    PantallaDatosPedido(
                        estado = estadoDatos,
                        unidades = carrito.totalUnidades,
                        total = carrito.total,
                        alCambiarTipo = modelo::cambiarTipo,
                        alCambiarMesa = modelo::cambiarMesa,
                        alCambiarNombre = modelo::cambiarNombre,
                        alCambiarTelefono = modelo::cambiarTelefono,
                        alCambiarDireccion = modelo::cambiarDireccion,
                        alCambiarObservaciones = modelo::cambiarObservaciones,
                        alEnviar = modelo::enviar,
                        alVolver = { pantalla = Pantalla.Carrito },
                        modifier = modificador,
                    )
                }
                Pantalla.Confirmado -> {
                    val id = pedidoConfirmadoId
                    if (id == null) {
                        // No debería pasar: sin pedido no hay nada que confirmar.
                        LaunchedEffect(Unit) { pantalla = Pantalla.Inicio }
                    } else {
                        val modelo = viewModel<PedidoConfirmadoViewModel>(key = "pedido-$id") {
                            PedidoConfirmadoViewModel(
                                repositorio = Dependencias.repositorioPedidos(),
                                pedidoId = id,
                                pedidoInicial = pedidoConfirmado?.takeIf { it.id == id },
                            )
                        }
                        // Bucle cancelable: se detiene al salir de la pantalla o
                        // cuando el estado del pedido es final.
                        LaunchedEffect(modelo) {
                            modelo.sondear()
                        }
                        val alcance = rememberCoroutineScope()
                        PantallaPedidoConfirmado(
                            estado = modelo.estado,
                            alActualizar = { alcance.launch { modelo.actualizar() } },
                            alVolverAlInicio = { pantalla = Pantalla.Inicio },
                            modifier = modificador,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el [CatalogoViewModel] compartido por el catálogo, el detalle, el
 * carrito y los datos del pedido (así todos ven las mismas pizzas y volver
 * atrás no recarga la carta) y lanza su carga si hace falta. Un
 * [versionServidor] distinto crea un modelo nuevo; un [intento] distinto
 * repite la carga tras un error.
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
