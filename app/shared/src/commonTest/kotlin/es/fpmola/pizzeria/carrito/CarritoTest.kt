package es.fpmola.pizzeria.carrito

import es.fpmola.pizzeria.modelo.Importe
import es.fpmola.pizzeria.prueba.carritoConDosPizzas
import es.fpmola.pizzeria.prueba.pizzaPrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarritoTest {

    // --- Añadir --------------------------------------------------------------

    @Test
    fun alEmpezarEstaVacio() {
        val carrito = Carrito()

        assertTrue(carrito.estaVacio)
        assertEquals(0, carrito.totalUnidades)
        assertEquals(Importe.CERO, carrito.total)
    }

    @Test
    fun anadirCreaUnaLineaConLosDatosDeLaPizza() {
        val carrito = Carrito()
        val pizza = pizzaPrueba(id = 7, precioCentimos = 950, nombre = "Margherita")

        val resultado = carrito.anadir(pizza, 2)

        assertEquals(ResultadoAnadir.Anadida, resultado)
        val linea = carrito.lineas.single()
        assertEquals(7, linea.pizzaId)
        assertEquals("Margherita", linea.nombre)
        assertEquals(Importe(950), linea.precioUnitario)
        assertEquals(2, linea.cantidad)
    }

    @Test
    fun anadirLaMismaPizzaSumaCantidadSinDuplicarLinea() {
        val carrito = Carrito()
        val pizza = pizzaPrueba(id = 1)

        carrito.anadir(pizza, 2)
        carrito.anadir(pizza, 3)

        assertEquals(1, carrito.lineas.size)
        assertEquals(5, carrito.cantidadDe(1))
    }

    @Test
    fun laCantidadMinimaAlAnadirEsUno() {
        val carrito = Carrito()

        carrito.anadir(pizzaPrueba(id = 1), 0)
        carrito.anadir(pizzaPrueba(id = 2), -4)

        assertEquals(1, carrito.cantidadDe(1))
        assertEquals(1, carrito.cantidadDe(2))
    }

    @Test
    fun laCantidadMaximaPorPizzaEsVeinte() {
        val carrito = Carrito()
        val pizza = pizzaPrueba(id = 1)

        assertEquals(ResultadoAnadir.Anadida, carrito.anadir(pizza, 20))
        assertEquals(20, carrito.cantidadDe(1))

        // Ya está en el máximo: no se añade nada más y se avisa.
        assertEquals(ResultadoAnadir.LimiteAlcanzado, carrito.anadir(pizza, 1))
        assertEquals(20, carrito.cantidadDe(1))
    }

    @Test
    fun alPasarseDelMaximoSeAnadenSoloLasQueCaben() {
        val carrito = Carrito()
        val pizza = pizzaPrueba(id = 1)
        carrito.anadir(pizza, 15)

        assertEquals(ResultadoAnadir.Anadida, carrito.anadir(pizza, 5))
        assertEquals(20, carrito.cantidadDe(1))

        val otra = pizzaPrueba(id = 2)
        carrito.anadir(otra, 18)
        assertEquals(ResultadoAnadir.LimiteAlcanzado, carrito.anadir(otra, 5))
        assertEquals(20, carrito.cantidadDe(2))
    }

    @Test
    fun pedirMuchasMasDeLasPermitidasSeAvisaYSeLimita() {
        val carrito = Carrito()

        val resultado = carrito.anadir(pizzaPrueba(id = 1), Int.MAX_VALUE)

        assertEquals(ResultadoAnadir.LimiteAlcanzado, resultado)
        assertEquals(Carrito.CANTIDAD_MAXIMA, carrito.cantidadDe(1))
    }

    @Test
    fun unaPizzaAgotadaNoSeAnade() {
        val carrito = Carrito()

        val resultado = carrito.anadir(pizzaPrueba(id = 1, disponible = false), 1)

        assertEquals(ResultadoAnadir.NoDisponible, resultado)
        assertTrue(carrito.estaVacio)
    }

    // --- Cambiar cantidad, quitar, vaciar ------------------------------------

    @Test
    fun cambiarCantidadFijaLaCantidadDentroDeLosLimites() {
        val carrito = carritoConDosPizzas()

        carrito.cambiarCantidad(1, 7)
        assertEquals(7, carrito.cantidadDe(1))

        carrito.cambiarCantidad(1, 0)
        assertEquals(Carrito.CANTIDAD_MINIMA, carrito.cantidadDe(1))

        carrito.cambiarCantidad(1, 99)
        assertEquals(Carrito.CANTIDAD_MAXIMA, carrito.cantidadDe(1))
    }

    @Test
    fun cambiarCantidadNoQuitaLaLineaNiAnadeUnaInexistente() {
        val carrito = carritoConDosPizzas()

        carrito.cambiarCantidad(1, 0)
        carrito.cambiarCantidad(99, 5)

        assertEquals(listOf(1, 3), carrito.lineas.map { it.pizzaId })
        assertEquals(0, carrito.cantidadDe(99))
    }

    @Test
    fun quitarEliminaSoloEsaLinea() {
        val carrito = carritoConDosPizzas()

        carrito.quitar(1)

        assertEquals(listOf(3), carrito.lineas.map { it.pizzaId })
    }

    @Test
    fun vaciarDejaElCarritoSinLineas() {
        val carrito = carritoConDosPizzas()

        carrito.vaciar()

        assertTrue(carrito.estaVacio)
        assertEquals(Importe.CERO, carrito.total)
    }

    // --- Total y unidades ----------------------------------------------------

    @Test
    fun calculaElTotalEnImporteYLasUnidades() {
        // 2 × 9,50 € + 1 × 13,50 € = 32,50 €
        val carrito = carritoConDosPizzas()

        assertEquals(3, carrito.totalUnidades)
        assertEquals(Importe(3250), carrito.total)
        assertEquals("32,50 €", carrito.total.formatear())
        assertEquals(Importe(1900), carrito.lineas.first().subtotal)
    }

    @Test
    fun elTotalSeActualizaAlCambiarLasCantidades() {
        val carrito = carritoConDosPizzas()

        carrito.cambiarCantidad(3, 2)

        // 2 × 9,50 € + 2 × 13,50 € = 46,00 €
        assertEquals(Importe(4600), carrito.total)
        assertEquals(4, carrito.totalUnidades)
    }

    // --- Ajuste a la carta ---------------------------------------------------

    @Test
    fun sincronizarQuitaLasPizzasQueYaNoEstanOEstanAgotadas() {
        val carrito = carritoConDosPizzas()
        val cartaNueva = listOf(
            pizzaPrueba(id = 1, precioCentimos = 950, nombre = "Margherita Clásica"),
            pizzaPrueba(id = 3, precioCentimos = 1350, disponible = false, nombre = "Cuatro Quesos Cremosa"),
        )

        val quitadas = carrito.sincronizarConCarta(cartaNueva)

        assertEquals(listOf("Cuatro Quesos Cremosa"), quitadas)
        assertEquals(listOf(1), carrito.lineas.map { it.pizzaId })
        assertEquals(listOf("Cuatro Quesos Cremosa"), carrito.quitadasPorCarta)
    }

    @Test
    fun sincronizarQuitaLasPizzasQueYaNoExisten() {
        val carrito = carritoConDosPizzas()

        val quitadas = carrito.sincronizarConCarta(listOf(pizzaPrueba(id = 1, precioCentimos = 950)))

        assertEquals(1, quitadas.size)
        assertEquals(listOf(1), carrito.lineas.map { it.pizzaId })
    }

    @Test
    fun sincronizarActualizaElPrecioYConservaLaCantidad() {
        val carrito = carritoConDosPizzas()

        val quitadas = carrito.sincronizarConCarta(
            listOf(
                pizzaPrueba(id = 1, precioCentimos = 1000),
                pizzaPrueba(id = 3, precioCentimos = 1350),
            ),
        )

        assertTrue(quitadas.isEmpty())
        assertEquals(Importe(1000), carrito.lineas.first { it.pizzaId == 1 }.precioUnitario)
        assertEquals(2, carrito.cantidadDe(1))
        assertTrue(carrito.quitadasPorCarta.isEmpty())
    }

    @Test
    fun descartarAvisoLimpiaLasPizzasQuitadas() {
        val carrito = carritoConDosPizzas()
        carrito.sincronizarConCarta(emptyList())
        assertEquals(2, carrito.quitadasPorCarta.size)

        carrito.descartarAviso()

        assertTrue(carrito.quitadasPorCarta.isEmpty())
    }
}
