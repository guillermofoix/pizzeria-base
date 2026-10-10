package es.fpmola.pizzeria.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Dependencias comunes a todas las plataformas.
 *
 * Cada plataforma aporta su propio módulo con lo que depende de ella
 * (por ejemplo, [es.fpmola.pizzeria.ajustes.AjustesServidor]).
 */
val moduloComun: Module = module {
}

/** Lista completa de módulos de la app a partir del módulo de la plataforma. */
fun modulosApp(moduloPlataforma: Module): List<Module> = listOf(moduloPlataforma, moduloComun)
