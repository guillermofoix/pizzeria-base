package es.fpmola.pizzeria.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Jerarquía de textos de la web de clientes (frontend-qr-app/style.css), con
// 1 rem = 16 px = 16 sp. La web usa Outfit para títulos y precios y Plus
// Jakarta Sans para el texto; la app usa la fuente del sistema (no se añaden
// archivos de fuente), con los mismos tamaños y pesos.
private val Base = Typography()

val TipografiaPizzeria = Typography(
    displaySmall = Base.displaySmall,
    headlineMedium = Base.headlineMedium,
    // Nombre de la marca y títulos de pantalla: 1,15 rem / 800 (style.css:91-93).
    headlineSmall = Base.headlineSmall.copy(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.4).sp,
    ),
    // Encabezados de sección y cajones: 1,1 a 1,2 rem / 700 (:181-182 y :479-480).
    titleLarge = Base.titleLarge.copy(
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    // Nombre de la pizza: 1,05 rem / 700 (:237-238).
    titleMedium = Base.titleMedium.copy(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    // Precio: 1 rem / 800 (:222-223).
    titleSmall = Base.titleSmall.copy(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.sp,
    ),
    // Texto: 0,95 rem; campos y listas 0,85 rem; descripciones 0,8 rem con
    // interlineado 1,35 (:242-244 y :526).
    bodyLarge = Base.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    bodyMedium = Base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodySmall = Base.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.sp),
    // Botones: 0,85 a 0,95 rem / 700 (:430-431 y :608-609).
    labelLarge = Base.labelLarge.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    ),
    // Chips y etiquetas: 0,8 rem / 600 (:159-160 y :515-516).
    labelMedium = Base.labelMedium.copy(
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    // Lema bajo la marca: 0,7 rem (:97-98).
    labelSmall = Base.labelSmall.copy(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
)
