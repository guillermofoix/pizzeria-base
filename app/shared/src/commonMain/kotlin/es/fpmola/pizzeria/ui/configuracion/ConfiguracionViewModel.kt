package es.fpmola.pizzeria.ui.configuracion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.fpmola.pizzeria.ajustes.AjustesServidor
import es.fpmola.pizzeria.ajustes.ValidacionUrl
import es.fpmola.pizzeria.ajustes.validarUrlServidor
import es.fpmola.pizzeria.salud.RepositorioSalud
import es.fpmola.pizzeria.salud.ResultadoSalud
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Estado de la pantalla de configuración del servidor. */
data class EstadoConfiguracion(
    val url: String = "",
    val probando: Boolean = false,
    val mensaje: String? = null,
    val exito: Boolean = false,
    /** URL que ha superado la prueba de conexión; solo esa se puede guardar. */
    val urlVerificada: String? = null,
) {
    val puedeGuardar: Boolean
        get() = urlVerificada != null && !probando
}

/**
 * Lógica de la pantalla de configuración: validar la URL, probar la
 * conexión con /api/health y guardar la URL verificada.
 */
class ConfiguracionViewModel(
    private val ajustes: AjustesServidor,
    private val repositorioSalud: RepositorioSalud,
) : ViewModel() {

    var estado by mutableStateOf(EstadoConfiguracion(url = ajustes.obtenerUrlBase().orEmpty()))
        private set

    private var pruebaEnCurso: Job? = null

    /** El usuario edita la URL: se descarta cualquier resultado anterior. */
    fun cambiarUrl(texto: String) {
        pruebaEnCurso?.cancel()
        estado = EstadoConfiguracion(url = texto)
    }

    /** Valida la URL y, si es correcta, comprueba la salud del servidor. */
    fun probarConexion() {
        when (val validacion = validarUrlServidor(estado.url)) {
            is ValidacionUrl.NoValida -> {
                estado = estado.copy(mensaje = validacion.motivo, exito = false, urlVerificada = null)
            }
            is ValidacionUrl.Valida -> {
                pruebaEnCurso?.cancel()
                estado = estado.copy(
                    url = validacion.url,
                    probando = true,
                    mensaje = null,
                    exito = false,
                    urlVerificada = null,
                )
                pruebaEnCurso = viewModelScope.launch {
                    estado = when (val resultado = repositorioSalud.comprobar(validacion.url)) {
                        is ResultadoSalud.Correcta -> estado.copy(
                            probando = false,
                            exito = true,
                            mensaje = "Conexión correcta: el servidor y la base de datos funcionan.",
                            urlVerificada = validacion.url,
                        )
                        is ResultadoSalud.Fallida -> estado.copy(
                            probando = false,
                            exito = false,
                            mensaje = resultado.mensaje,
                        )
                    }
                }
            }
        }
    }

    /**
     * Guarda la URL verificada.
     *
     * @return true si se ha guardado; false si aún no hay una conexión correcta.
     */
    fun guardarUrl(): Boolean {
        val url = estado.urlVerificada ?: return false
        ajustes.guardarUrlBase(url)
        return true
    }
}
