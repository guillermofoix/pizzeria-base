package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.fpmola.pizzeria.ui.tema.coloresPizzeria

/**
 * Botón de texto con el rojo de acento del tema: el rojo primario no llega a
 * 4,5:1 de contraste como texto pequeño sobre las tarjetas oscuras.
 */
@Composable
fun BotonTexto(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.coloresPizzeria.acento),
        content = content,
    )
}
