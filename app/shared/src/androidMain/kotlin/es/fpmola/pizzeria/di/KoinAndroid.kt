package es.fpmola.pizzeria.di

import android.content.Context
import es.fpmola.pizzeria.ajustes.AjustesServidor
import es.fpmola.pizzeria.ajustes.AjustesServidorAndroid
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/** Dependencias propias de Android. */
private fun moduloAndroid(contexto: Context): Module = module {
    single<AjustesServidor> { AjustesServidorAndroid(contexto) }
}

/**
 * Arranca Koin. Se llama una sola vez, desde Application.onCreate().
 */
fun iniciarKoin(contexto: Context) {
    val contextoApp = contexto.applicationContext
    startKoin {
        modules(modulosApp(moduloAndroid(contextoApp)))
    }
}
