package es.fpmola.pizzeria.salud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de GET /api/health. NO usa el envoltorio RespuestaApi:
 * {"status":"UP","service":"pizzeria-backend","timestamp":"…",
 *  "database":{"connected":true,"db_time":"…"}}
 */
@Serializable
data class SaludServidor(
    @SerialName("status") val estado: String,
    @SerialName("service") val servicio: String? = null,
    @SerialName("timestamp") val marcaTiempo: String? = null,
    @SerialName("database") val baseDatos: SaludBaseDatos,
) {
    /** El servidor está operativo solo si dice "UP" y tiene la base de datos conectada. */
    val operativo: Boolean
        get() = estado == ESTADO_OPERATIVO && baseDatos.conectada

    companion object {
        const val ESTADO_OPERATIVO = "UP"
    }
}

/** Bloque "database" de la salud del servidor. */
@Serializable
data class SaludBaseDatos(
    @SerialName("connected") val conectada: Boolean,
    @SerialName("db_time") val horaBaseDatos: String? = null,
    @SerialName("error") val error: String? = null,
)
