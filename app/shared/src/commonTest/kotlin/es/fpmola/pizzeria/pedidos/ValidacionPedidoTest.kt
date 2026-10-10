package es.fpmola.pizzeria.pedidos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidacionPedidoTest {

    // --- En mesa -------------------------------------------------------------

    @Test
    fun mesaValidaSoloExigeElNumeroDeMesa() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = "4"))

        assertFalse(errores.hayErrores)
    }

    @Test
    fun mesaNoExigeNombreTelefonoNiDireccion() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = "12"))

        assertNull(errores.nombre)
        assertNull(errores.telefono)
        assertNull(errores.direccion)
    }

    @Test
    fun mesaSinNumeroEsObligatoria() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = "  "))

        assertEquals("Indica el número de mesa.", errores.mesa)
    }

    @Test
    fun mesaConFormatoNoValido() {
        for (texto in listOf("abc", "4a", "-3", "4.5", "1 2")) {
            val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = texto))
            assertEquals(
                "El número de mesa debe ser un número entero, por ejemplo 4.",
                errores.mesa,
                "Debería rechazar \"$texto\"",
            )
        }
    }

    @Test
    fun mesaFueraDeRango() {
        for (texto in listOf("0", "1000", "99999999999999")) {
            val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = texto))
            assertEquals(
                "El número de mesa debe estar entre 1 y 999.",
                errores.mesa,
                "Debería rechazar \"$texto\"",
            )
        }
    }

    @Test
    fun numeroDeMesaDevuelveElEnteroOnull() {
        assertEquals(4, numeroDeMesa(" 4 "))
        assertEquals(999, numeroDeMesa("999"))
        assertEquals(7, numeroDeMesa("007"))
        assertNull(numeroDeMesa(""))
        assertNull(numeroDeMesa("0"))
        assertNull(numeroDeMesa("1000"))
        assertNull(numeroDeMesa("x"))
    }

    // --- Para recoger --------------------------------------------------------

    @Test
    fun recogerValidoExigeNombreYTelefono() {
        val errores = validarDatos(
            DatosPedido(tipo = TipoPedido.Recoger, nombre = "Pedro", telefono = "600 000 003"),
        )

        assertFalse(errores.hayErrores)
    }

    @Test
    fun recogerNoExigeMesaNiDireccion() {
        val errores = validarDatos(
            DatosPedido(tipo = TipoPedido.Recoger, nombre = "Pedro", telefono = "600000003"),
        )

        assertNull(errores.mesa)
        assertNull(errores.direccion)
    }

    @Test
    fun recogerSinNombreNiTelefono() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Recoger))

        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertEquals("El teléfono es obligatorio.", errores.telefono)
        assertNull(errores.direccion)
    }

    // --- A domicilio ---------------------------------------------------------

    @Test
    fun domicilioValidoExigeNombreTelefonoYDireccion() {
        val errores = validarDatos(
            DatosPedido(
                tipo = TipoPedido.Domicilio,
                nombre = "Ana López",
                telefono = "+34 600 123 456",
                direccion = "Calle Mayor 1, 2º A",
            ),
        )

        assertFalse(errores.hayErrores)
    }

    @Test
    fun domicilioSinDatosExigeLosTresCampos() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Domicilio))

        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertEquals("El teléfono es obligatorio.", errores.telefono)
        assertEquals("La dirección de entrega es obligatoria.", errores.direccion)
        assertNull(errores.mesa)
    }

    @Test
    fun direccionDemasiadoCorta() {
        val errores = validarDatos(
            DatosPedido(
                tipo = TipoPedido.Domicilio,
                nombre = "Ana",
                telefono = "600123456",
                direccion = "Av 1",
            ),
        )

        assertEquals("Escribe la dirección completa (calle, número y piso).", errores.direccion)
    }

    // --- Teléfono y nombre ---------------------------------------------------

    @Test
    fun telefonosValidos() {
        for (telefono in listOf("600123456", "600 123 456", "600-123-456", "+34600123456", "(91) 123 45 67")) {
            val errores = validarDatos(
                DatosPedido(tipo = TipoPedido.Recoger, nombre = "Pedro", telefono = telefono),
            )
            assertNull(errores.telefono, "Debería aceptar \"$telefono\"")
        }
    }

    @Test
    fun telefonosNoValidos() {
        val mensaje = "Introduce un teléfono válido, por ejemplo 600 123 456."
        for (telefono in listOf("123", "60012345", "60012345a", "abcdefghi", "6001234567890123", "600+123456")) {
            val errores = validarDatos(
                DatosPedido(tipo = TipoPedido.Recoger, nombre = "Pedro", telefono = telefono),
            )
            assertEquals(mensaje, errores.telefono, "Debería rechazar \"$telefono\"")
        }
    }

    @Test
    fun normalizarTelefonoQuitaSeparadores() {
        assertEquals("600123456", normalizarTelefono("600 123-456"))
        assertEquals("+34600123456", normalizarTelefono("+34 600.123.456"))
        assertEquals("911234567", normalizarTelefono("(91) 123 45 67"))
    }

    @Test
    fun nombreDemasiadoLargo() {
        val largo = "a".repeat(NOMBRE_LONGITUD_MAXIMA + 1)

        val recoger = validarDatos(
            DatosPedido(tipo = TipoPedido.Recoger, nombre = largo, telefono = "600123456"),
        )
        val mesa = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = "1", nombre = largo))

        assertTrue(recoger.nombre!!.startsWith("El nombre es demasiado largo"))
        assertTrue(mesa.nombre!!.startsWith("El nombre es demasiado largo"))
    }

    @Test
    fun elNombreEnMesaEsOpcional() {
        val errores = validarDatos(DatosPedido(tipo = TipoPedido.Mesa, mesa = "3", nombre = ""))

        assertNull(errores.nombre)
    }
}
