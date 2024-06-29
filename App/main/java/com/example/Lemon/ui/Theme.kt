package com.example.arcus.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp




val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

)

@Composable
fun ArcusTheme(areDynamicColorsEnabled: Boolean = true, content: @Composable () -> Unit) {
    val doesDeviceSupportDynamicColors =
        areDynamicColorsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    val arcusColorScheme = darkColorScheme(
        // Define your new primary color
        primary = Color(0xFF4CAF50), // Green
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        inversePrimary = Color(0xFF9CCC65),

        // Define your new secondary color
        secondary = Color(0xFFE91E63), // Pink
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFD8E4),
        onSecondaryContainer = Color(0xFF8B0032),

        // Define your new tertiary color
        tertiary = Color(0xFF9C27B0), // Purple
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFEDE7F6),
        onTertiaryContainer = Color(0xFF4A148C),

        // Define your new error color
        error = Color(0xFFF44336), // Red
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF8B0000),

        // Define your new background and surface colors
        background = Color(0xFF121212), // Dark gray
        onBackground = Color.White,
        surface = Color(0xFF212121), // Slightly lighter dark gray
        onSurface = Color.White,
        inverseSurface = Color(0xFFF5F5F5), // Light gray
        inverseOnSurface = Color(0xFF303030), // Dark gray

        // Define any other colors you need
        surfaceVariant = Color(0xFF424242), // Gray
        onSurfaceVariant = Color.White,
        outline = Color(0xFF9E9E9E) // Light gray
    )

    MaterialTheme(
        colorScheme = if (doesDeviceSupportDynamicColors) dynamicDarkColorScheme(context)
        else arcusColorScheme,
        typography = Typography,
        content = content
    )
}
