package es.fpmola.pizzeria.ajustes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidacionUrlTest {

    @Test
    fun aceptaHttpsYQuitaLaBarraFinalYLosEspacios() {
        assertEquals(
            ValidacionUrl.Valida("https://dam-01.ejemplo.org"),
            validarUrlServidor("  https://dam-01.ejemplo.org//  "),
        )
    }

    @Test
    fun normalizaElPrefijoEnMayusculas() {
        assertEquals(
            ValidacionUrl.Valida("https://ejemplo.org"),
            validarUrlServidor("HTTPS://ejemplo.org/"),
        )
    }

    @Test
    fun rechazaHttpSinCifrar() {
        assertIs<ValidacionUrl.NoValida>(validarUrlServidor("http://ejemplo.org"))
    }

    @Test
    fun rechazaOtrosFormatos() {
        for (texto in listOf("", "   ", "ejemplo.org", "https://", "https:///ruta", "https://eje mplo.org", "https://ejemplo.org/?a=1")) {
            assertIs<ValidacionUrl.NoValida>(validarUrlServidor(texto), "Debería rechazar \"$texto\"")
        }
    }
}
