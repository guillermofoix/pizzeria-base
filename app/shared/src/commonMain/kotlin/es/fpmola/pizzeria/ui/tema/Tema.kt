package es.fpmola.pizzeria.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// PALETA DE LA PIZZERÍA, tomada de la web
//
// - Esquema OSCURO: la web de clientes, frontend-qr-app/src/style.css (:root).
// - Esquema CLARO: frontend-web (Tailwind: paleta "brand" y grises "slate").
//
// Todos los roles de Material 3 se definen a mano, incluidos los de superficie,
// para que no aparezca ningún tono por defecto (el lavanda de Material).
// Cuando un color de la web no llega a 4,5:1 de contraste con su texto, se
// ajusta lo mínimo y se indica en el comentario.
// =============================================================================

/**
 * Colores de la web que Material 3 no tiene como rol: el rojo de acento para
 * texto pequeño y los colores de los estados de un pedido.
 *
 * Se obtienen con `MaterialTheme.coloresPizzeria`.
 */
@Immutable
class ColoresPizzeria(
    /** Rojo legible como texto (el primario no llega a 4,5:1 sobre las tarjetas oscuras). */
    val acento: Color,
    val estadoPendiente: Color,
    val estadoPreparacion: Color,
    val estadoReparto: Color,
    val estadoListo: Color,
    val estadoEntregado: Color,
    val estadoCancelado: Color,
)

private val ColoresOscuros = darkColorScheme(
    // Rojo de marca: #e63946 (style.css:16) oscurecido a #dd2e3d para que el
    // texto blanco llegue a 4,5:1 (con #e63946 solo llega a 4,17).
    primary = Color(0xFFDD2E3D),
    onPrimary = Color(0xFFFFFFFF),
    // rgba(230, 57, 70, 0.15) sobre la tarjeta #161b22 (style.css:500 y :10).
    primaryContainer = Color(0xFF352027),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFD62828),

    // Ámbar #f59e0b (style.css:18): totales y subtotales.
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF0D1117),
    // Ámbar al 15 % sobre la tarjeta.
    secondaryContainer = Color(0xFF382F1E),
    onSecondaryContainer = Color(0xFFF59E0B),

    // Verde #10b981 (style.css:19): éxito.
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF0D1117),
    // Verde al 15 % sobre la tarjeta.
    tertiaryContainer = Color(0xFF153330),
    onTertiaryContainer = Color(0xFF10B981),

    // Rojo claro #f87171 (index.html:103 y main.js:143).
    error = Color(0xFFF87171),
    onError = Color(0xFF0D1117),
    errorContainer = Color(0xFF38282E),
    onErrorContainer = Color(0xFFF87171),

    // Fondo #0d1117 y texto #f0f6fc (style.css:9 y :21).
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFF0F6FC),
    surface = Color(0xFF0D1117),
    onSurface = Color(0xFFF0F6FC),
    // Tarjeta #161b22 (:10) y texto atenuado #8b949e (:22).
    surfaceVariant = Color(0xFF161B22),
    onSurfaceVariant = Color(0xFF8B949E),
    // Sin tinte: ninguna elevación pinta el color primario encima.
    surfaceTint = Color(0x00000000),

    inverseSurface = Color(0xFFF0F6FC),
    inverseOnSurface = Color(0xFF0D1117),

    // Borde de campos: el "tenue" #6e7681 (style.css:23) en lugar de #30363d
    // (:14), que solo da 1,55:1. Borde de tarjetas y separadores: #21262d (:13).
    outline = Color(0xFF6E7681),
    outlineVariant = Color(0xFF21262D),
    scrim = Color(0xFF000000),

    // Escala de superficies, de la más oscura a la más clara.
    surfaceDim = Color(0xFF0D1117),
    surfaceBright = Color(0xFF1F242C),
    surfaceContainerLowest = Color(0xFF0B0E14), // campos de texto (:12)
    surfaceContainerLow = Color(0xFF0D1117), // fondo
    surfaceContainer = Color(0xFF161B22), // tarjetas (:10)
    surfaceContainerHigh = Color(0xFF1F242C), // tarjeta al pasar el dedo (:11)
    surfaceContainerHighest = Color(0xFF21262D),
)

private val ColoresClaros = lightColorScheme(
    // Rojo #d62828 (brand-600, frontend-web index.html:33): blanco da 5,01:1.
    // El brand-500 #e63946 solo da 4,17:1.
    primary = Color(0xFFD62828),
    onPrimary = Color(0xFFFFFFFF),
    // brand-100 y brand-700 (index.html:31 y :34).
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFFBA181B),
    inversePrimary = Color(0xFFF87171),

    // Ámbar: #f59e0b (index.html:35) sobre blanco solo da 2,15:1; se usa
    // #b45309 (amber-700, 5,02:1).
    secondary = Color(0xFFB45309),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),

    // Verde: #10b981 (index.html:37) sobre blanco solo da 2,54:1; se usa
    // #047857 (emerald-700, 5,48:1).
    tertiary = Color(0xFF047857),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),

    // Rojo #dc2626, el color del tema de la web QR (index.html:7).
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    // Fondo slate-50 y texto slate-900 (index.html:145).
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    // slate-100 y slate-600.
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = Color(0x00000000),

    inverseSurface = Color(0xFF0F172A),
    inverseOnSurface = Color(0xFFF8FAFC),

    // Borde de campos: slate-500 (4,76:1) en lugar de slate-300 (1,48:1).
    // Borde de tarjetas y separadores: slate-200, como la web.
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0xFF000000),

    // Escala de superficies: las tarjetas de la web son blancas.
    surfaceDim = Color(0xFFE2E8F0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFE2E8F0),
)

// Estados de pedido de frontend-web/src/main.js:1608-1631. En oscuro se usan los
// tonos 500 de la web, salvo el morado y el gris, que no llegan a 4,5:1 sobre la
// tarjeta. En claro se usan los tonos 700, porque los 500 no llegan sobre blanco.
// "Cancelado" no tiene color en la web: se usa el de error.
private val EstadosOscuros = ColoresPizzeria(
    acento = Color(0xFFF87171),
    estadoPendiente = Color(0xFFF59E0B),
    estadoPreparacion = Color(0xFF3B82F6),
    estadoReparto = Color(0xFFC084FC),
    estadoListo = Color(0xFF10B981),
    estadoEntregado = Color(0xFF94A3B8),
    estadoCancelado = Color(0xFFF87171),
)

private val EstadosClaros = ColoresPizzeria(
    acento = Color(0xFFBA181B),
    estadoPendiente = Color(0xFFB45309),
    estadoPreparacion = Color(0xFF1D4ED8),
    estadoReparto = Color(0xFF7E22CE),
    estadoListo = Color(0xFF047857),
    estadoEntregado = Color(0xFF475569),
    estadoCancelado = Color(0xFFDC2626),
)

private val LocalColoresPizzeria = staticCompositionLocalOf { EstadosClaros }

/** Colores propios de la pizzería (acento y estados del pedido). */
val MaterialTheme.coloresPizzeria: ColoresPizzeria
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresPizzeria.current

/**
 * Tema Material 3 de la pizzería: colores de la web (claro y oscuro según el
 * sistema), tipografía de [TipografiaPizzeria] y radios de [FormasPizzeria].
 */
@Composable
fun TemaPizzeria(
    oscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColoresPizzeria provides if (oscuro) EstadosOscuros else EstadosClaros,
    ) {
        MaterialTheme(
            colorScheme = if (oscuro) ColoresOscuros else ColoresClaros,
            typography = TipografiaPizzeria,
            shapes = FormasPizzeria,
            content = contenido,
        )
    }
}
