package es.fpmola.pizzeria.di

import es.fpmola.pizzeria.ajustes.AjustesServidor
import es.fpmola.pizzeria.carrito.Carrito
import es.fpmola.pizzeria.catalogo.RepositorioCatalogo
import es.fpmola.pizzeria.pagos.AbridorUrl
import es.fpmola.pizzeria.pagos.RepositorioPagos
import es.fpmola.pizzeria.pedidos.RepositorioPedidos
import es.fpmola.pizzeria.salud.RepositorioSalud
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Acceso a las dependencias de Koin desde la interfaz.
 *
 * Se usa koin-core directamente (sin koin-compose) para no añadir más
 * dependencias ligadas a la versión de Compose.
 */
internal object Dependencias : KoinComponent {
    fun ajustes(): AjustesServidor = get()
    fun repositorioSalud(): RepositorioSalud = get()
    fun repositorioCatalogo(): RepositorioCatalogo = get()
    fun repositorioPedidos(): RepositorioPedidos = get()
    fun repositorioPagos(): RepositorioPagos = get()
    fun abridorUrl(): AbridorUrl = get()
    fun carrito(): Carrito = get()
}
