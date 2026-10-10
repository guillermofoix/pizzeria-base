package es.fpmola.pizzeria.pagos

import es.fpmola.pizzeria.prueba.AbridorFalso
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AbridorUrlTest {

    @Test
    fun aceptaLasUrlsHttpsDeStripe() {
        val validas = listOf(
            "https://checkout.stripe.com/c/pay/cs_test_a1B2c3#fidkdWxOYHwnPyd1blpxYHZxWjA0",
            "https://stripe.com",
            "https://stripe.com/es",
            "https://buy.stripe.com/test_abc?prefilled_email=a%40b.com",
            "https://pay.checkout.stripe.com:443/x",
            "HTTPS://CHECKOUT.STRIPE.COM/c/pay/x",
        )
        for (url in validas) {
            assertTrue(esUrlSegura(url), url)
        }
    }

    @Test
    fun rechazaLosHostsQueNoSonDeStripe() {
        val invalidas = listOf(
            "https://evil.com",
            "https://evil.com/stripe.com",
            "https://evil.com/?u=checkout.stripe.com",
            "https://evil.com/#checkout.stripe.com",
            "https://stripe.com.evil.com/pay",
            "https://checkout.stripe.com.evil.com/pay",
            "https://evilstripe.com",
            "https://notstripe.com/pay",
            "https://stripe.co",
            "https://stripe.com-evil.com",
            "https://.stripe.com",
            "https://a..stripe.com",
        )
        for (url in invalidas) {
            assertFalse(esUrlSegura(url), url)
        }
    }

    @Test
    fun rechazaLosTrucosConUsuarioPuertoOCaracteresRaros() {
        val invalidas = listOf(
            "https://checkout.stripe.com@evil.com/pay",
            "https://checkout.stripe.com:pass@evil.com/pay",
            "https://evil.com\\@checkout.stripe.com/pay",
            "https://checkout.stripe.com\\.evil.com/pay",
            "https://checkout.stripe.com:abc/pay",
            "https://checkout.stripe.com:/pay",
            "https://checkout.stripe.com:123456/pay",
            "https://checkout.stripe.com /pay",
            "https://checkout.stripe.com\n.evil.com/pay",
            "https://checkout.stripe.com%2eevil.com/pay",
            "https://chéckout.stripe.com/pay",
            "https://checkout.stripe.com.",
        )
        for (url in invalidas) {
            assertFalse(esUrlSegura(url), url)
        }
    }

    @Test
    fun rechazaLosEsquemasQueNoSonHttps() {
        val invalidas = listOf(
            "http://checkout.stripe.com/pay",
            "ftp://checkout.stripe.com/pay",
            "javascript:alert(1)",
            "intent://checkout.stripe.com/#Intent;scheme=https;end",
            "checkout.stripe.com/pay",
            "//checkout.stripe.com/pay",
            "https:checkout.stripe.com",
            "https:/checkout.stripe.com",
            " https://checkout.stripe.com/pay",
            "",
            "https://",
        )
        for (url in invalidas) {
            assertFalse(esUrlSegura(url), url)
        }
    }

    @Test
    fun elHostDeStripeSeComparaSinMayusculasYSoloConPuntoDeSubdominio() {
        assertTrue(esHostDeStripe("stripe.com"))
        assertTrue(esHostDeStripe("Checkout.Stripe.COM"))
        assertFalse(esHostDeStripe(null))
        assertFalse(esHostDeStripe(""))
        assertFalse(esHostDeStripe("estripe.com"))
        assertFalse(esHostDeStripe("stripe.com.evil.com"))
    }

    @Test
    fun elAbridorAbreSoloLasUrlsDeStripe() {
        val abridor = AbridorFalso()

        val abierta = abridor.abrir("https://checkout.stripe.com/c/pay/cs_test_1")
        val rechazadaHost = abridor.abrir("https://evil.com/pay")
        val rechazadaEsquema = abridor.abrir("http://checkout.stripe.com/pay")

        assertEquals(ResultadoAbrirUrl.Abierta, abierta)
        assertEquals(listOf("https://checkout.stripe.com/c/pay/cs_test_1"), abridor.abiertas)
        assertEquals(MENSAJE_URL_NO_SEGURA, assertIs<ResultadoAbrirUrl.Fallida>(rechazadaHost).mensaje)
        assertIs<ResultadoAbrirUrl.Fallida>(rechazadaEsquema)
    }
}
