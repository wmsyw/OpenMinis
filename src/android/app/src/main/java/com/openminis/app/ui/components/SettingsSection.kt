package com.openminis.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A titled settings group using the shared section typography, panel, and spacing.
 * Pair with [SettingsRowDivider] between the caller's content rows.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = SectionDesign.SectionTopGap),
    ) {
        SectionHeader(text = title)
        SectionCard(content = content)
    }
}

/** Shared inset divider for rows inside a [SettingsSection]. */
@Composable
fun SettingsRowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = SectionDesign.DividerStartInset),
        thickness = SectionDesign.DividerThickness,
        color = SectionDesign.dividerColor(),
    )
}
