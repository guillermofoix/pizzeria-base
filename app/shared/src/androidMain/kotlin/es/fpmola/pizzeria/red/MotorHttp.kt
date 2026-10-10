package es.fpmola.pizzeria.red

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

/**
 * Motor HTTP de Android (OkHttp) con tiempos de espera razonables para
 * redes móviles.
 */
fun crearMotorHttp(): HttpClientEngine = OkHttp.create {
    config {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(20, TimeUnit.SECONDS)
        writeTimeout(20, TimeUnit.SECONDS)
        // OkHttp reintenta por su cuenta algunas conexiones fallidas. Se desactiva
        // para que un POST (crear un pedido) nunca se repita sin que el usuario lo sepa.
        retryOnConnectionFailure(false)
    }
}
