package com.example.app_brote

// Etapas de crecimiento de la planta
enum class EtapaCrecimiento(val nombre: String, val icono: String) {
    SEMILLA("Semilla", "🌱"),
    BROTE("Brote", "🌿"),
    PLANTA_CON_HOJAS("Planta con hojas", "🪴"),
    FLOR("Flor desarrollada", "🌸")
}

// Catálogo de ilustraciones de flores disponibles (flor_1 a flor_5)
val FLORES_DISPONIBLES = listOf(
    R.drawable.flor_1,
    R.drawable.flor_2,
    R.drawable.flor_3,
    R.drawable.flor_4,
    R.drawable.flor_5
)

// Obtiene el recurso de la flor asignada según su tipo (1..5)
fun recursoFlorPorTipo(tipo: Int): Int {
    val index = (tipo - 1).coerceIn(0, FLORES_DISPONIBLES.size - 1)
    return FLORES_DISPONIBLES[index]
}

// Data class de la Meta de ahorro
data class Meta(
    val id: Long,
    val nombre: String,
    val objetivo: Double,
    val ahorrado: Double = 0.0,
    val florTipo: Int = (((Math.abs(id) % FLORES_DISPONIBLES.size) + 1).toInt())
) {
    // Progreso de 0.0 a 1.0 (0% a 100%)
    val progreso: Float
        get() = if (objetivo <= 0) 0f else (ahorrado / objetivo).toFloat().coerceIn(0f, 1f)

    // Cuánto falta (nunca negativo)
    val restante: Double
        get() = (objetivo - ahorrado).coerceAtLeast(0.0)

    // ¿Ya llegué a la meta?
    val completada: Boolean
        get() = ahorrado >= objetivo

    // Etapa de crecimiento según el porcentaje de ahorro
    val etapa: EtapaCrecimiento
        get() = when {
            progreso >= 1f -> EtapaCrecimiento.FLOR
            progreso >= 0.66f -> EtapaCrecimiento.PLANTA_CON_HOJAS
            progreso > 0f -> EtapaCrecimiento.BROTE
            else -> EtapaCrecimiento.SEMILLA
        }

    val florResId: Int
        get() = recursoFlorPorTipo(florTipo)
}

// Modelo de flor desbloqueada en el jardín con el nombre de la meta asociada
data class FlorDesbloqueada(
    val id: Long,
    val metaId: Long,
    val florTipo: Int,
    val florResId: Int = recursoFlorPorTipo(florTipo),
    val pagina: Int,
    val parcela: Int,
    val fechaDesbloqueo: Long = System.currentTimeMillis(),
    val metaNombre: String = ""
)

// Representación de una parcela con coordenadas normalizadas (0f..1f)
// correspondientes a las dimensiones del fondo (602x1200 px)
data class ParcelaPosicion(
    val id: Int,
    val xNorm: Float, // Centro X normalizado (0f..1f)
    val yNorm: Float  // Centro Y normalizado (0f..1f)
)

// Número de parcelas por página de jardín (cuadrícula 3 x 4)
const val PARCELAS_POR_PAGINA = 12

// Parcelas ubicadas en el lecho central de tierra fértil de fondojardin
// Las filas se elevaron ligeramente (y = 0.28, 0.42, 0.56, 0.70)
// para respetar el espacio café sin acercarse demasiado a la hierba inferior.
val PARCELAS_JARDIN: List<ParcelaPosicion> = run {
    val columnas = 3
    val filas = 4
    val xCoords = listOf(0.24f, 0.50f, 0.76f)
    val yCoords = listOf(0.28f, 0.42f, 0.56f, 0.70f)
    val lista = mutableListOf<ParcelaPosicion>()
    var id = 0
    for (row in 0 until filas) {
        for (col in 0 until columnas) {
            lista.add(ParcelaPosicion(id = id++, xNorm = xCoords[col], yNorm = yCoords[row]))
        }
    }
    lista
}

// Elige la planta según el progreso (compatibilidad)
fun plantaPara(progreso: Float): String = when {
    progreso >= 1f -> "🌸"
    progreso >= 0.66f -> "🪴"
    progreso > 0f -> "🌿"
    else -> "🌱"
}

// Convierte 1500.0 en "$1500"
fun dinero(valor: Double): String = "\$" + valor.toLong()
