package com.openminis.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.openminis.app.ui.theme.ChatColors

/**
 * Shared settings rhythm for native Material pages: cool, quiet grouped panels,
 * readable labels, and content-driven rows. Keep these tokens shared so provider,
 * model, network, permission, and other settings pages stay visually consistent.
 */
object SectionDesign {
    val ScreenHorizontalPadding = 16.dp
    val HeadingHorizontalPadding = 24.dp
    val CardShape = RoundedCornerShape(20.dp)
    val ControlShape = RoundedCornerShape(14.dp)
    val FirstSectionTopGap = 16.dp
    val SectionTopGap = 24.dp
    val HeaderToCardGap = 8.dp
    val CardToFooterGap = 8.dp
    val InterRowGap = 8.dp
    val RowMinHeight = 52.dp
    val TwoLineRowMinHeight = 64.dp
    val TextFieldMinHeight = 56.dp
    val RowHorizontalPadding = 16.dp
    val RowVerticalPadding = 8.dp
    val RowIconSize = 28.dp
    val RowIconTextGap = 12.dp
    val CardInnerVerticalPadding = 4.dp
    val DividerThickness = 0.5.dp
    val DividerStartInset = 16.dp

    @Composable
    @ReadOnlyComposable
    fun cardColor(): Color = Color.White.copy(alpha = if (ChatColors.isDark) 0.05f else 0.68f)

    @Composable
    @ReadOnlyComposable
    fun cardBorderColor(): Color = Color.White.copy(alpha = if (ChatColors.isDark) 0.07f else 0.52f)

    @Composable
    @ReadOnlyComposable
    fun rowTitleStyle(): TextStyle = MaterialTheme.typography.bodyLarge.let {
        it.copy(fontSize = it.fontSize * (15f / 16f), lineHeight = it.lineHeight * (21f / 26f))
    }

    @Composable
    @ReadOnlyComposable
    fun rowSubtitleStyle(): TextStyle = MaterialTheme.typography.bodySmall.let {
        it.copy(fontSize = it.fontSize * (13f / 12f), lineHeight = it.lineHeight * (19f / 18f))
    }

    @Composable
    @ReadOnlyComposable
    fun screenBackgroundColor(): Color = MaterialTheme.colorScheme.background

    @Composable
    @ReadOnlyComposable
    fun dividerColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)

    @Composable
    @ReadOnlyComposable
    fun footerColor(): Color = ChatColors.tertiaryText
}

/** Quiet section caption, inset slightly from the panel's outer gutter. */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = ChatColors.tertiaryText,
        modifier = modifier.padding(
            start = SectionDesign.HeadingHorizontalPadding,
            end = SectionDesign.HeadingHorizontalPadding,
            bottom = SectionDesign.HeaderToCardGap,
        ).semantics { heading() },
    )
}

/** Supporting information below a settings panel; never line-clamped. */
@Composable
fun SectionFooter(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = SectionDesign.rowSubtitleStyle(),
        color = SectionDesign.footerColor(),
        modifier = modifier.padding(
            start = SectionDesign.HeadingHorizontalPadding,
            end = SectionDesign.HeadingHorizontalPadding,
            top = SectionDesign.CardToFooterGap,
        ),
    )
}

/** Shared, lightly filled panel for rows separated by [SectionDivider]. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = SectionDesign.cardColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = SectionDesign.CardShape,
        border = BorderStroke(SectionDesign.DividerThickness, SectionDesign.cardBorderColor()),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SectionDesign.ScreenHorizontalPadding),
    ) {
        Column(
            modifier = Modifier.padding(vertical = SectionDesign.CardInnerVerticalPadding),
            content = content,
        )
    }
}

/** Divider between rows inside a [SectionCard]. */
@Composable
fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = SectionDesign.DividerStartInset),
        thickness = SectionDesign.DividerThickness,
        color = SectionDesign.dividerColor(),
    )
}

/**
 * Inline label placed directly above a [SectionTextField] (or
 * [SectionDropdown]) when several labelled fields share a section but
 * each row needs its own short caption — too small a scope for a full
 * [SectionHeader]. Used by ModelEntryDetail / AddCustomModel /
 * AddProvider for fields like "Model ID" / "Display Name" / "Context Window".
 *
 * Labels sit outside the field so large text never overlaps a floating label.
 *
 * Horizontal padding is 0: callsites place this inside [SettingsCardBlock]
 * (or a manually padded Column) which already adds the 16dp inset. Adding
 * our own 16dp on top would double-indent the label relative to sibling
 * SectionHeader / description text / radio rows — see T353. Mirrors
 * SectionTextField's contentPadding=0 from T352.
 */
@Composable
fun RowLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 6.dp),
    )
}
