package es.fpmola.pizzeria.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Radios de borde de la web de clientes (frontend-qr-app/style.css:26-29):
 * 8 dp para campos e imágenes pequeñas, 14 dp para tarjetas, 20 dp para
 * hojas inferiores. Los botones y los chips son píldoras (radio completo) y
 * se redondean por completo en cada componente.
 */
val FormasPizzeria = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
