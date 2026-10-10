package es.fpmola.pizzeria.pedidos

import es.fpmola.pizzeria.carrito.LineaCarrito
import es.fpmola.pizzeria.modelo.RespuestaApi
import es.fpmola.pizzeria.red.ClienteApi
import es.fpmola.pizzeria.red.ErrorRed
import es.fpmola.pizzeria.red.jsonApi
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable

/**
 * Construye el cuerpo de POST /api/pedidos a partir de lo escrito por el
 * usuario y de las líneas del carrito.
 *
 * Solo envía los campos que corresponden al tipo de pedido y los ids de pizza
 * del carrito (que vienen de la carta). No envía ningún precio: el servidor los
 * calcula. El teléfono se envía sin espacios ni guiones y como texto.
 */
fun construirPeticion(datos: DatosPedido, lineas: List<LineaCarrito>): PeticionPedido {
    val esMesa = datos.tipo == TipoPedido.Mesa
    return PeticionPedido(
        tipoPedido = datos.tipo.valorApi,
        mesaNumero = if (esMesa) numeroDeMesa(datos.mesa) else null,
        clienteNombre = datos.nombre.trim().ifEmpty { null },
        clienteTelefono = if (esMesa) null else normalizarTelefono(datos.telefono.trim()),
        clienteDireccion = if (datos.tipo == TipoPedido.Domicilio) datos.direccion.trim() else null,
        metodoPago = if (datos.pagoConTarjeta) METODO_PAGO_TARJETA else datos.tipo.metodoPago,
        observaciones = datos.observaciones.trim().ifEmpty { null },
        lineas = lineas.map { LineaPeticion(pizzaId = it.pizzaId, cantidad = it.cantidad) },
    )
}

/**
 * Mensaje para cuando no se sabe si el pedido llegó al servidor: explica que
 * quizá sí se registró y que hay que comprobarlo antes de volver a enviarlo.
 */
fun mensajeEnvioIncierto(motivo: String): String =
    "$motivo Es posible que el pedido sí se haya registrado. Antes de volver a enviarlo, " +
        "comprueba con el local si lo ha recibido, para evitar un pedido duplicado. " +
        "Tu carrito se mantiene."

/** Resultado de intentar crear un pedido. */
sealed interface ResultadoEnvioPedido {

    /** El servidor creó el pedido. */
    data class Creado(val pedido: Pedido) : ResultadoEnvioPedido

    /** El servidor rechazó el pedido (error 4xx): seguro que no se creó. */
    data class Rechazado(val mensaje: String, val codigo: Int) : ResultadoEnvioPedido

    /** No se sabe si el pedido llegó (sin conexión, error 5xx o respuesta ilegible). */
    data class Incierto(val mensaje: String) : ResultadoEnvioPedido

    /** La petición no llegó a salir (no hay servidor configurado). */
    data class NoEnviado(val mensaje: String) : ResultadoEnvioPedido
}

/** Cuerpo de error del backend: solo interesa el mensaje legible. */
@Serializable
private data class CuerpoError(val message: String? = null)

/**
 * Crea pedidos (POST /api/pedidos) y los consulta (GET /api/pedidos/{id}).
 *
 * Crear un pedido NUNCA se reintenta automáticamente.
 */
class RepositorioPedidos(private val cliente: ClienteApi) {

    /**
     * Envía el pedido una sola vez. No lanza [ErrorRed]: todos los desenlaces
     * se devuelven como [ResultadoEnvioPedido], con mensajes en español.
     */
    suspend fun crearPedido(peticion: PeticionPedido): ResultadoEnvioPedido {
        val cuerpo = jsonApi.encodeToString(PeticionPedido.serializer(), peticion)
        val respuesta = try {
            cliente.peticionPostJson(RUTA_PEDIDOS, cuerpo)
        } catch (e: ErrorRed.SinServidorConfigurado) {
            return ResultadoEnvioPedido.NoEnviado(e.mensajeUsuario)
        } catch (e: ErrorRed.SinConexion) {
            return ResultadoEnvioPedido.Incierto(
                mensajeEnvioIncierto("No hemos podido confirmar si el pedido ha llegado."),
            )
        }

        val codigo = respuesta.status.value
        return when {
            respuesta.status.isSuccess() -> interpretarRespuestaCorrecta(respuesta)
            codigo in 400..499 -> ResultadoEnvioPedido.Rechazado(
                mensaje = mensajeDelServidor(respuesta)
                    ?: "El servidor no ha aceptado el pedido (código $codigo).",
                codigo = codigo,
            )
            else -> ResultadoEnvioPedido.Incierto(
                mensajeEnvioIncierto("El servidor ha tenido un problema (código $codigo)."),
            )
        }
    }

    /**
     * Consulta un pedido (GET /api/pedidos/{id}).
     *
     * @throws ErrorRed si no hay servidor, no hay conexión, el servidor falla
     * o la respuesta no tiene el formato esperado.
     */
    suspend fun obtenerPedido(id: Int): Pedido {
        val respuesta = cliente.obtener<RespuestaApi<Pedido>>("$RUTA_PEDIDOS/$id")
        val datos = respuesta.data
        if (!respuesta.success || datos == null) throw ErrorRed.RespuestaInvalida()
        return datos
    }

    private suspend fun interpretarRespuestaCorrecta(respuesta: HttpResponse): ResultadoEnvioPedido {
        val pedido = try {
            val cuerpo = jsonApi.decodeFromString(
                RespuestaApi.serializer(Pedido.serializer()),
                respuesta.bodyAsText(),
            )
            cuerpo.data?.takeIf { cuerpo.success }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return if (pedido != null) {
            ResultadoEnvioPedido.Creado(pedido)
        } else {
            // El servidor respondió con éxito pero no se entiende la respuesta:
            // el pedido pudo crearse.
            ResultadoEnvioPedido.Incierto(
                mensajeEnvioIncierto("El servidor ha respondido, pero la app no ha entendido la respuesta."),
            )
        }
    }

    /** Mensaje legible del cuerpo de un error, o null si no se puede leer. */
    private suspend fun mensajeDelServidor(respuesta: HttpResponse): String? =
        try {
            jsonApi.decodeFromString(CuerpoError.serializer(), respuesta.bodyAsText())
                .message
                ?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    companion object {
        const val RUTA_PEDIDOS = "/api/pedidos"
    }
}
