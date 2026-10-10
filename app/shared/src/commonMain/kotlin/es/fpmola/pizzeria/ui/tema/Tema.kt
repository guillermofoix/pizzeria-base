package es.fpmola.pizzeria.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de la pizzería: rojo tomate, naranja y crema.
private val RojoTomate = Color(0xFFC62828)
private val RojoTomateClaro = Color(0xFFFFDAD4)
private val RojoTomateOscuro = Color(0xFF410001)
private val RojoTomateNoche = Color(0xFFFFB4A8)
private val RojoTomateNocheContenedor = Color(0xFF93000A)

private val Naranja = Color(0xFFE65100)
private val NaranjaClaro = Color(0xFFFFDBC8)
private val NaranjaOscuro = Color(0xFF331200)
private val NaranjaNoche = Color(0xFFFFB68E)
private val NaranjaNocheContenedor = Color(0xFF7A3000)

private val Crema = Color(0xFFFFF8EC)
private val CremaVariante = Color(0xFFF3E6D3)
private val TextoSobreCrema = Color(0xFF2B1A12)
private val TextoSecundarioSobreCrema = Color(0xFF5C4A3E)

private val FondoNoche = Color(0xFF1E1612)
private val FondoNocheVariante = Color(0xFF3A2D25)
private val TextoNoche = Color(0xFFF3E6D3)
private val TextoSecundarioNoche = Color(0xFFD8C2B3)

private val ColoresClaros = lightColorScheme(
    primary = RojoTomate,
    onPrimary = Color.White,
    primaryContainer = RojoTomateClaro,
    onPrimaryContainer = RojoTomateOscuro,
    secondary = Naranja,
    onSecondary = Color.White,
    secondaryContainer = NaranjaClaro,
    onSecondaryContainer = NaranjaOscuro,
    background = Crema,
    onBackground = TextoSobreCrema,
    surface = Crema,
    onSurface = TextoSobreCrema,
    surfaceVariant = CremaVariante,
    onSurfaceVariant = TextoSecundarioSobreCrema,
)

private val ColoresOscuros = darkColorScheme(
    primary = RojoTomateNoche,
    onPrimary = RojoTomateOscuro,
    primaryContainer = RojoTomateNocheContenedor,
    onPrimaryContainer = RojoTomateClaro,
    secondary = NaranjaNoche,
    onSecondary = NaranjaOscuro,
    secondaryContainer = NaranjaNocheContenedor,
    onSecondaryContainer = NaranjaClaro,
    background = FondoNoche,
    onBackground = TextoNoche,
    surface = FondoNoche,
    onSurface = TextoNoche,
    surfaceVariant = FondoNocheVariante,
    onSurfaceVariant = TextoSecundarioNoche,
)

/**
 * Tema Material 3 de la pizzería. Sigue el modo claro u oscuro del sistema.
 */
@Composable
fun TemaPizzeria(
    oscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) ColoresOscuros else ColoresClaros,
        content = contenido,
    )
}
