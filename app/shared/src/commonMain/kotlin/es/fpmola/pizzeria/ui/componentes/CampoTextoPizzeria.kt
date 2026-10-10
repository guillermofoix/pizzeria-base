package es.fpmola.pizzeria.ui.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Campo de texto como los de la web: fondo de campo del tema, borde fino que
 * se vuelve rojo al enfocarlo y esquinas de 8 dp. Muestra el error debajo.
 */
@Composable
fun CampoTextoPizzeria(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    unaLinea: Boolean = true,
    opcionesTeclado: KeyboardOptions = KeyboardOptions.Default,
    accionesTeclado: KeyboardActions = KeyboardActions.Default,
    placeholder: String? = null,
) {
    val fondoCampo = MaterialTheme.colorScheme.surfaceContainerLowest
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        label = { Text(etiqueta) },
        placeholder = placeholder?.let { texto -> { Text(texto) } },
        isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        singleLine = unaLinea,
        keyboardOptions = opcionesTeclado,
        keyboardActions = accionesTeclado,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fondoCampo,
            unfocusedContainerColor = fondoCampo,
            disabledContainerColor = fondoCampo,
            errorContainerColor = fondoCampo,
        ),
    )
}
