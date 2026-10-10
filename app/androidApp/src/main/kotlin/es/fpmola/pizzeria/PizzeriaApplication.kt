package es.fpmola.pizzeria

import android.app.Application
import es.fpmola.pizzeria.di.iniciarKoin

/**
 * Punto de entrada del proceso: prepara la inyección de dependencias.
 */
class PizzeriaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        iniciarKoin(this)
    }
}
