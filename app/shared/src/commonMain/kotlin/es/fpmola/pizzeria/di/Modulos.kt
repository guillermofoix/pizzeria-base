package es.fpmola.pizzeria.di

import es.fpmola.pizzeria.red.ClienteApi
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Dependencias comunes a todas las plataformas.
 *
 * Cada plataforma aporta su propio módulo con lo que depende de ella:
 * [es.fpmola.pizzeria.ajustes.AjustesServidor] y el motor HTTP de Ktor.
 */
val moduloComun: Module = module {
    single { ClienteApi(ajustes = get(), motor = get()) }
}

/** Lista completa de módulos de la app a partir del módulo de la plataforma. */
fun modulosApp(moduloPlataforma: Module): List<Module> = listOf(moduloPlataforma, moduloComun)
