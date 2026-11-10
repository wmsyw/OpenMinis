package com.openminis.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic chat colors for the mist-blue / deep-slate Material theme.
// Resolved from LocalChatPalette so in-app appearance and export overrides
// share the same neutral surfaces, readable ink, and primary action accent.
@Immutable
data class ChatPalette(
    val isDark: Boolean,
    val background: Color,
    val secondaryBg: Color,
    val inputBg: Color,
    val inputIconBg: Color,
    val inputIconBorder: Color,
    val inputBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val disabledText: Color,
    val userBubble: Color,
    val toolBg: Color,
    val toolBorder: Color,
    val toolCapsuleBg: Color,
    val separator: Color,
    val sendButton: Color,
    val sendButtonDisabled: Color,
    val codeBlockBg: Color,
    val codeBlockText: Color,
    val inlineCodeBg: Color,
    val inlineCodeText: Color,
    val link: Color,
    val blockquoteBar: Color,
    val thinking: Color,
    val warningBg: Color,
    val warningText: Color,
    val tableBorder: Color,
    val inputShadow: Color,
    val toastBg: Color,
    val thumbnailBorder: Color,
    val sheetHeaderBg: Color,
    val sheetHeaderBorder: Color,
    val fabAccent: Color,
)

val LightChatPalette = ChatPalette(
    isDark = false,
    background = Color(0xFFF0F4FD),
    secondaryBg = Color(0xFFEEF1F9),
    inputBg = Color(0xFFFBFCFF),
    inputIconBg = Color(0xFFEEF1F9),
    inputIconBorder = Color(0xFFDCE0E8),
    inputBorder = Color(0xFF7E838D),
    primaryText = Color(0xFF1E232E),
    secondaryText = Color(0xFF4F5562),
    tertiaryText = Color(0xFF646974),
    disabledText = Color(0xFFA1A5AC),
    userBubble = Color(0xFFD9E4FD),
    toolBg = Color(0xFFF6F9FE),
    toolBorder = Color(0xFFDCE0E8),
    toolCapsuleBg = Color(0xFFEEF1F9),
    separator = Color(0xFFDCE0E8),
    sendButton = Color(0xFF4B67B4),
    sendButtonDisabled = Color(0xFFA1A5AC),
    codeBlockBg = Color(0xFFEEF1F9),
    codeBlockText = Color(0xFF1E232E),
    inlineCodeBg = Color(0xFFE7EBF4),
    inlineCodeText = Color(0xFF1E232E),
    link = Color(0xFF4B67B4),
    blockquoteBar = Color(0xFF7E838D),
    thinking = Color(0xFF4B67B4),
    warningBg = Color(0xFFFFF4DA),
    warningText = Color(0xFF76551E),
    tableBorder = Color(0xFFDCE0E8),
    inputShadow = Color.Transparent,
    toastBg = Color(0xFFDEE8FF),
    thumbnailBorder = Color(0xFFDCE0E8),
    sheetHeaderBg = Color(0xFFFBFCFF),
    sheetHeaderBorder = Color(0xFFDCE0E8),
    fabAccent = Color(0xFF4B67B4),
)

// Opaque slate layers keep the composer, tool panels, and code wells distinct
// without pure-black backgrounds, luminous code ink, or elevated tint.
val DarkChatPalette = ChatPalette(
    isDark = true,
    background = Color(0xFF1C1F28),
    secondaryBg = Color(0xFF232730),
    inputBg = Color(0xFF272C36),
    inputIconBg = Color(0xFF343944),
    inputIconBorder = Color(0xFF343841),
    inputBorder = Color(0xFF737781),
    primaryText = Color(0xFFE6E9F0),
    secondaryText = Color(0xFFB6BBC5),
    tertiaryText = Color(0xFF9A9EA9),
    disabledText = Color(0xFF656971),
    userBubble = Color(0xFF293144),
    toolBg = Color(0xFF272C36),
    toolBorder = Color(0xFF343841),
    toolCapsuleBg = Color(0xFF2D323C),
    separator = Color(0xFF343841),
    sendButton = Color(0xFF97B1F0),
    sendButtonDisabled = Color(0xFF656971),
    codeBlockBg = Color(0xFF14171E),
    codeBlockText = Color(0xFFE6E9F0),
    inlineCodeBg = Color(0xFF343944),
    inlineCodeText = Color(0xFFE6E9F0),
    link = Color(0xFF97B1F0),
    blockquoteBar = Color(0xFF737781),
    thinking = Color(0xFF97B1F0),
    warningBg = Color(0xFF3B3222),
    warningText = Color(0xFFE1C18A),
    tableBorder = Color(0xFF343841),
    inputShadow = Color.Transparent,
    toastBg = Color(0xFF2E3C5F),
    thumbnailBorder = Color(0xFF343841),
    sheetHeaderBg = Color(0xFF272C36),
    sheetHeaderBorder = Color(0xFF343841),
    fabAccent = Color(0xFF97B1F0),
)

val LocalChatPalette = compositionLocalOf { LightChatPalette }

// Short accessor: ChatColors.primaryText instead of LocalChatPalette.current.primaryText
val ChatColors: ChatPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalChatPalette.current
