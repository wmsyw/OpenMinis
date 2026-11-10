package com.openminis.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.openminis.app.R
import com.openminis.app.ui.components.SectionDesign
import com.openminis.app.ui.components.SectionFooter
import com.openminis.app.ui.components.SectionHeader

/**
 * Shared native Material settings primitives with quiet, tonal grouped panels.
 *
 * Structure:
 *   SettingsScaffold(title, actions?) {
 *     SettingsSection(header?, footer?) { SettingsRow/SwitchRow/ValueRow(...) ... }
 *     SettingsSection(...) { ... }
 *   }
 */

// ─── Scaffold ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    // [T-android-settings-ui-md3] #11 onBack is nullable so an EDIT screen can
    // suppress the back arrow and use an explicit Cancel/Save action pair instead
    // (a back arrow + a "Cancel" action that both pop the screen is redundant and
    // semantically muddy). Non-edit screens keep passing a non-null onBack and get
    // the usual back arrow — unchanged.
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    // Optional navigation and centered title support existing editing screens.
    navigation: @Composable (() -> Unit)? = null,
    centerTitle: Boolean = false,
    floatingActionButton: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    // [T-android-storage-usage-cache] Optional bottom action bar (e.g. a
    // selection mode's actions). Null keeps every existing caller unchanged.
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            val titleSlot: @Composable () -> Unit = {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            val navigationSlot: @Composable () -> Unit = {
                when {
                    navigation != null -> navigation()
                    onBack != null -> IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            }
            if (centerTitle) {
                CenterAlignedTopAppBar(
                    title = titleSlot,
                    navigationIcon = navigationSlot,
                    actions = { actions?.invoke() },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            } else {
                TopAppBar(
                    title = titleSlot,
                    navigationIcon = navigationSlot,
                    actions = { actions?.invoke() },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        bottomBar = { bottomBar?.invoke() },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        // T183: imePadding() shrinks the scroll container by the IME's
        // height while the keyboard is up, giving Modifier.bringIntoView()
        // (used by `bringIntoViewOnFocus`) a meaningful "above the
        // keyboard" rect to scroll a focused TextField into. Without it,
        // adjustResize + edge-to-edge leaves the scrollable column at
        // full height behind the IME and bringIntoView is a no-op.
        val baseMod = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
        Column(
            modifier = if (scrollable) baseMod.verticalScroll(rememberScrollState()) else baseMod,
            content = content,
        )
    }
}

// ─── Switch ────────────────────────────────────────────────────────────────────

/**
 * [T-android-switch-row-height] A [Switch] that does not inflate the row it
 * sits in. Use this instead of Material's `Switch` inside any settings row.
 *
 * ## The problem it solves
 *
 * Compose applies `LocalMinimumInteractiveComponentSize` (48dp) to Switch, so
 * although the control DRAWS at 32dp it MEASURES at 48. Inside a row whose
 * height is `heightIn(min = 56.dp)` + 12dp vertical padding, that produced:
 *
 *     with a switch:  48 + 12 + 12  = 72dp   (heightIn never applies)
 *     without one:    30 + 12 + 12  = 54dp   -> clamped to 56dp
 *
 * Measured on a Pixel 6: switch rows rendered 190px and plain rows 147px, a
 * visible 16dp step between neighbouring rows of the same card. It read as
 * "the Encryption section has odd spacing" when in fact every switch row in
 * the app was taller than every non-switch row.
 *
 * Row height remains content-driven so labels and controls can grow with font scale.
 *
 * ## Why waiving the minimum is safe here
 *
 * The 48dp floor exists so a small control is still easy to hit. In these rows
 * the WHOLE ROW is clickable and at least 56dp tall, and tapping anywhere on it
 * toggles the same state, so the effective touch target grows rather than
 * shrinks. Material's own guidance allows waiving the minimum exactly when a
 * control is embedded in a larger interactive surface.
 *
 * Do NOT use this for a standalone switch that is the only touch target.
 */
@Composable
fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: androidx.compose.material3.SwitchColors = SwitchDefaults.colors(),
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
        )
    }
}

// ─── Section ───────────────────────────────────────────────────────────────────

/**
 * A grouped section with a readable heading, rounded panel, and optional footer.
 * Children (SettingsRow / SettingsSwitchRow / …) appear inside the card; dividers auto-inset.
 */
@Composable
fun SettingsSection(
    header: String? = null,
    footer: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = SectionDesign.SectionTopGap),
    ) {
        if (header != null) {
            SectionHeader(text = header)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SectionDesign.ScreenHorizontalPadding)
                .clip(SectionDesign.CardShape)
                .background(SectionDesign.cardColor())
                .padding(vertical = SectionDesign.CardInnerVerticalPadding),
            content = content,
        )
        if (footer != null) {
            SectionFooter(text = footer)
        }
    }
}

// ─── Row primitives ────────────────────────────────────────────────────────────

/**
 * Generic row: tonal icon, wrapping title/subtitle, trailing slot, and optional chevron.
 * Pass `showDivider = false` on the last row of a section.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    showDivider: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
    // [T-android-settings-ui-md3] #8 single-line List Item is 56dp; a caller with
    // two-line content (e.g. the model list: name + id) passes 72dp for the MD3
    // double-line height. Default keeps every other row at the single-line 56dp.
    minHeight: Dp = 56.dp,
    // Long-press on a row that is also tappable (e.g. a skill's context menu).
    // Null keeps the plain clickable, so no other row changes.
    onLongClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // [T-android-settings-ui-md3] #1/#8 fixed List Item height so every
                // toggle/value/nav row in a section is uniform (was content-driven
                // → 24/54/72dp mix). heightIn(min) not height() so an unexpectedly
                // tall row can still grow; the symmetric 12dp vertical padding (was
                // effectively asymmetric once the 0.5dp divider was added/removed)
                // is what made a no-subtitle last row read ~50px shorter.
                .heightIn(min = minHeight)
                .then(
                    when {
                        onClick != null && onLongClick != null ->
                            Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                        onClick != null -> Modifier.clickable(onClick = onClick)
                        else -> Modifier
                    },
                )
                .padding(horizontal = SectionDesign.RowHorizontalPadding, vertical = SectionDesign.RowVerticalPadding),
            // #10 keep the trailing control (Switch/value) vertically centered
            // against the title — already centered, kept explicit.
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = lerp(MaterialTheme.colorScheme.primary, iconColor, 0.15f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }

            if (showChevron) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        if (showDivider) {
            val insetStart = if (icon != null) 60.dp else SectionDesign.DividerStartInset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = insetStart, end = SectionDesign.RowHorizontalPadding)
                    .height(SectionDesign.DividerThickness)
                    .background(SectionDesign.dividerColor()),
            )
        }
    }
}

/** Title + Switch row. */
@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    showDivider: Boolean = true,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconColor = iconColor,
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
        showChevron = false,
        showDivider = showDivider,
        trailing = {
            SettingsSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(),
            )
        },
    )
}

/** Title + right-aligned value text (tap opens picker/detail). */
@Composable
fun SettingsValueRow(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconColor = iconColor,
        onClick = onClick,
        showChevron = onClick != null,
        showDivider = showDivider,
        trailing = {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                modifier = Modifier.widthIn(max = 144.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        },
    )
}

/** Single-choice row (tap to select; shows check on selected). Used for radio-style lists. */
@Composable
fun SettingsChoiceRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // [T-android-settings-ui-md3] #1 match SettingsRow's 56dp min so
                // choice/radio rows line up with toggle/value rows in mixed lists.
                .heightIn(min = 56.dp)
                .clickable(onClick = onSelect)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = SectionDesign.DividerStartInset, end = SectionDesign.RowHorizontalPadding)
                    .height(SectionDesign.DividerThickness)
                    .background(SectionDesign.dividerColor()),
            )
        }
    }
}

/**
 * Container for non-row content (sliders, segmented pickers, custom composables)
 * that still wants the grouped-card background.
 */
@Composable
fun SettingsCardBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        content = content,
    )
}
