package com.openminis.app.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Shared settings rhythm for native Material pages: cool, quiet grouped panels,
 * readable labels, and content-driven rows. Keep these tokens shared so provider,
 * model, network, permission, and other settings pages stay visually consistent.
 */
object SectionDesign {
    val ScreenHorizontalPadding = 16.dp
    val CardShape = RoundedCornerShape(20.dp)
    val ControlShape = RoundedCornerShape(14.dp)
    val FirstSectionTopGap = 16.dp
    val SectionTopGap = 24.dp
    val HeaderToCardGap = 8.dp
    val CardToFooterGap = 8.dp
    val InterRowGap = 8.dp
    val RowMinHeight = 48.dp
    val TextFieldMinHeight = 56.dp
    val RowHorizontalPadding = 16.dp
    val RowVerticalPadding = 12.dp
    val CardInnerVerticalPadding = 4.dp
    val DividerThickness = 0.5.dp
    val DividerStartInset = 16.dp

    @Composable
    @ReadOnlyComposable
    fun cardColor(): Color = MaterialTheme.colorScheme.surfaceContainerLow

    @Composable
    @ReadOnlyComposable
    fun screenBackgroundColor(): Color = MaterialTheme.colorScheme.background

    @Composable
    @ReadOnlyComposable
    fun dividerColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    @Composable
    @ReadOnlyComposable
    fun footerColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant
}

/** Sentence-case section title, aligned to the panel's outer gutter. */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(
            start = SectionDesign.ScreenHorizontalPadding,
            end = SectionDesign.ScreenHorizontalPadding,
            bottom = SectionDesign.HeaderToCardGap,
        ),
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
        style = MaterialTheme.typography.bodySmall,
        color = SectionDesign.footerColor(),
        modifier = modifier.padding(
            start = SectionDesign.ScreenHorizontalPadding,
            end = SectionDesign.ScreenHorizontalPadding,
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
        shape = SectionDesign.CardShape,
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
