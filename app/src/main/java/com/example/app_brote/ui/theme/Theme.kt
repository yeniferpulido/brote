package com.example.app_brote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VerdeBosquePrimario,
    onPrimary = TextoInvertido,
    primaryContainer = VerdeMentaMuySuave,
    onPrimaryContainer = TextoPrimario,

    secondary = VerdeHojaSecundario,
    onSecondary = TextoInvertido,
    secondaryContainer = SuperficieContenedor,
    onSecondaryContainer = TextoPrimario,

    tertiary = DoradoAcento,
    onTertiary = TextoInvertido,
    tertiaryContainer = CremaTierra,
    onTertiaryContainer = TextoPrimario,

    background = FondoPantalla,
    onBackground = TextoPrimario,

    surface = SuperficieTarjeta,
    onSurface = TextoPrimario,
    surfaceVariant = SuperficieContenedor,
    onSurfaceVariant = TextoSecundario,

    outline = BordeDelicado,
    error = EstadoError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeMentaMarca,
    onPrimary = TextoPrimario,
    primaryContainer = VerdeBosquePrimario,
    onPrimaryContainer = Color.White,

    secondary = VerdeMentaClaro,
    onSecondary = TextoPrimario,
    secondaryContainer = Color(0xFF1B3C2B),
    onSecondaryContainer = Color.White,

    tertiary = DoradoAcento,
    onTertiary = TextoPrimario,

    background = Color(0xFF0F1E16),
    onBackground = Color(0xFFE5F2EA),

    surface = Color(0xFF172B20),
    onSurface = Color(0xFFE5F2EA),
    surfaceVariant = Color(0xFF1F382B),
    onSurfaceVariant = Color(0xFF9EBFAC),

    outline = Color(0xFF2C4A3A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun App_BroteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para mantener la identidad visual de fondoinicio
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
