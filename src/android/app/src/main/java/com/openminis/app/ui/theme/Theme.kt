package com.openminis.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mist-blue accent and cool slate surfaces, shared with the chat palette.
// Every Material role is explicit so native controls never fall back to the
// stock purple scheme. Surface hierarchy comes from the neutral ramp, not tint.
private val LightColorScheme = lightColorScheme(
    primary = LightChatPalette.sendButton,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE8FF),
    onPrimaryContainer = Color(0xFF213262),
    inversePrimary = DarkChatPalette.sendButton,
    secondary = Color(0xFF545E6F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E6F0),
    onSecondaryContainer = LightChatPalette.primaryText,
    tertiary = Color(0xFF6B578F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFECE5FB),
    onTertiaryContainer = Color(0xFF3B2C54),
    background = LightChatPalette.background,
    onBackground = LightChatPalette.primaryText,
    surface = LightChatPalette.background,
    onSurface = LightChatPalette.primaryText,
    surfaceVariant = Color(0xFFE7EBF4),
    onSurfaceVariant = LightChatPalette.secondaryText,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF2D323C),
    inverseOnSurface = Color(0xFFE6E9F0),
    error = Color(0xFFA54743),
    onError = Color(0xFFFDF9F8),
    errorContainer = Color(0xFFFFE2E0),
    onErrorContainer = Color(0xFF642724),
    outline = Color(0xFF7E838D),
    outlineVariant = LightChatPalette.separator,
    scrim = Color(0xFF0B111F),
    surfaceBright = Color(0xFFFBFCFF),
    surfaceDim = Color(0xFFDFE4EE),
    surfaceContainerLowest = Color(0xFFFBFCFF),
    surfaceContainerLow = Color(0xFFF6F9FE),
    surfaceContainer = Color(0xFFEEF1F9),
    surfaceContainerHigh = Color(0xFFE7EBF4),
    surfaceContainerHighest = Color(0xFFDFE4EE),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkChatPalette.sendButton,
    onPrimary = Color(0xFF121C34),
    primaryContainer = Color(0xFF2E3C5F),
    onPrimaryContainer = Color(0xFFD8E1F6),
    inversePrimary = LightChatPalette.sendButton,
    secondary = Color(0xFFB3BED2),
    onSecondary = Color(0xFF1A2230),
    secondaryContainer = Color(0xFF343944),
    onSecondaryContainer = DarkChatPalette.primaryText,
    tertiary = Color(0xFFBEAEDE),
    onTertiary = Color(0xFF251C36),
    tertiaryContainer = Color(0xFF3E3451),
    onTertiaryContainer = Color(0xFFE4DDF3),
    background = DarkChatPalette.background,
    onBackground = DarkChatPalette.primaryText,
    surface = DarkChatPalette.background,
    onSurface = DarkChatPalette.primaryText,
    surfaceVariant = Color(0xFF343944),
    onSurfaceVariant = DarkChatPalette.secondaryText,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFE6E9F0),
    inverseOnSurface = Color(0xFF1E232E),
    error = Color(0xFFE69089),
    onError = Color(0xFF331513),
    errorContainer = Color(0xFF542C29),
    onErrorContainer = Color(0xFFF8D9D7),
    outline = Color(0xFF737781),
    outlineVariant = DarkChatPalette.separator,
    scrim = Color(0xFF0B111F),
    surfaceBright = Color(0xFF343944),
    surfaceDim = Color(0xFF14171E),
    surfaceContainerLowest = Color(0xFF14171E),
    surfaceContainerLow = Color(0xFF232730),
    surfaceContainer = Color(0xFF272C36),
    surfaceContainerHigh = Color(0xFF2D323C),
    surfaceContainerHighest = Color(0xFF343944),
)

// FABs and send actions share the accent, including in-app appearance overrides.
@Composable
fun minisFabColor(): Color = LocalChatPalette.current.fabAccent

// Shared 6 / 10 / 14 / 20 / 28dp shape vocabulary.
private val MinisShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MinisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = remember(fontScale) { scaledTypography(fontScale) }
    val chatPalette = if (darkTheme) DarkChatPalette else LightChatPalette

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MinisShapes,
        typography = typography,
    ) {
        CompositionLocalProvider(LocalChatPalette provides chatPalette, content = content)
    }
}

/**
 * [T-multi-select-share] Re-themes a subtree light or dark regardless of the app's
 * own appearance, keeping the app's typography (font scale) and shapes. The chat
 * export uses it so a dark app can share a light page and the other way round:
 * every chat row reads its colours from MaterialTheme / LocalChatPalette.
 */
@Composable
fun MinisAppearance(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
    ) {
        CompositionLocalProvider(
            LocalChatPalette provides if (darkTheme) DarkChatPalette else LightChatPalette,
            content = content,
        )
    }
}

private fun TextStyle.scale(factor: Float): TextStyle =
    if (factor == 1f) this else copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)

/**
 * [T-fa-locale] Each paragraph takes its direction from its own text, but
 * stays left-aligned.
 *
 * The app keeps a left-to-right layout in every language (LocaleWrap.forceLtr),
 * and under an LTR layout Compose otherwise lays every paragraph out LTR too.
 * Persian text that mixes in Latin words ("کلیدهای API و ورود با OAuth") then
 * comes out in left-to-right word order, which a Persian reader reads
 * scrambled. Content direction fixes the word order; Left (not Start, which
 * would follow an RTL paragraph to the right edge) keeps every label where it
 * sits today. For left-to-right text both are what it already had.
 */
private fun TextStyle.bidi(): TextStyle =
    copy(textDirection = TextDirection.Content, textAlign = TextAlign.Left)

// System sans throughout; monospace remains reserved for rendered code.
// Explicit line heights scale with the app preference as well as system sp.
private val MinisTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 43.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 38.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 35.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 29.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
)

private fun scaledTypography(factor: Float): Typography {
    val base = MinisTypography
    return Typography(
        displayLarge = base.displayLarge.scale(factor).bidi(),
        displayMedium = base.displayMedium.scale(factor).bidi(),
        displaySmall = base.displaySmall.scale(factor).bidi(),
        headlineLarge = base.headlineLarge.scale(factor).bidi(),
        headlineMedium = base.headlineMedium.scale(factor).bidi(),
        headlineSmall = base.headlineSmall.scale(factor).bidi(),
        titleLarge = base.titleLarge.scale(factor).bidi(),
        titleMedium = base.titleMedium.scale(factor).bidi(),
        titleSmall = base.titleSmall.scale(factor).bidi(),
        bodyLarge = base.bodyLarge.scale(factor).bidi(),
        bodyMedium = base.bodyMedium.scale(factor).bidi(),
        bodySmall = base.bodySmall.scale(factor).bidi(),
        labelLarge = base.labelLarge.scale(factor).bidi(),
        labelMedium = base.labelMedium.scale(factor).bidi(),
        labelSmall = base.labelSmall.scale(factor).bidi(),
    )
}
