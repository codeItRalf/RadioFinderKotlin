import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Base colors
private val AlmostBlack = Color(0xFF121212)
private val DarkGray = Color(0xFF1F1F1F)
private val White = Color(0xFFFFFFFF)

// Neon Colors
private val NeonPink = Color(0xFFFF1B8D)
private val NeonBlue = Color(0xFF00F3FF)
private val NeonPurple = Color(0xFFBF00FF)
private val NeonYellow = Color(0xFFFFE300)
private val NeonGreen = Color(0xFF39FF14)

// Muted versions for containers
private val MutedPink = Color(0xFF590031)
private val MutedBlue = Color(0xFF004C52)
private val MutedPurple = Color(0xFF3A0052)

private val CustomColorScheme = darkColorScheme(
    // Primary colors (Pink theme)
    primary = NeonPink,
    onPrimary = White,
    primaryContainer = MutedPink,
    onPrimaryContainer = NeonPink,

    // Secondary colors (Blue theme)
    secondary = NeonBlue,
    onSecondary = AlmostBlack,
    secondaryContainer = MutedBlue,
    onSecondaryContainer = NeonBlue,

    // Tertiary colors (Purple theme)
    tertiary = NeonPurple,
    onTertiary = White,
    tertiaryContainer = MutedPurple,
    onTertiaryContainer = NeonPurple,

    // Background colors
    background = AlmostBlack,
    onBackground = White,

    // Surface colors
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCACACA),

    // Error colors
    error = NeonYellow,
    onError = AlmostBlack,
    errorContainer = Color(0xFF524900),
    onErrorContainer = NeonYellow,

    // Other colors
    outline = NeonPink.copy(alpha = 0.5f),
    outlineVariant = NeonBlue.copy(alpha = 0.3f),
    scrim = AlmostBlack.copy(alpha = 0.3f)
)

// Define the Typography
val Typography = Typography(
    // Title styles
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun RadioFinderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content
    )
}