package com.example.app_brote

// Una "data class" es una ficha que guarda datos.
// Cada meta tiene: id, nombre, cuánto quiero ahorrar y cuánto llevo.
data class Meta(
    val id: Long,
    val nombre: String,
    val objetivo: Double,
    val ahorrado: Double = 0.0
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
}

// Elige la planta según el progreso (después puedes cambiar los emojis por imágenes tuyas)
fun plantaPara(progreso: Float): String = when {
    progreso >= 1f -> "🌸"
    progreso >= 0.75f -> "🌳"
    progreso >= 0.5f -> "🪴"
    progreso >= 0.25f -> "🌿"
    else -> "🌱"
}

// Convierte 1500.0 en "$1500"
fun dinero(valor: Double): String = "\$" + valor.toLong()

