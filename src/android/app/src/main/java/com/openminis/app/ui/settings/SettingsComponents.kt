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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.openminis.app.R
import com.openminis.app.ui.components.SectionDesign
import com.openminis.app.ui.components.SectionCard
import com.openminis.app.ui.components.SectionFooter
import com.openminis.app.ui.components.SectionHeader
import com.openminis.app.ui.theme.ChatColors
import com.openminis.app.ui.theme.MinisBackdrop

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
    // Editing/selection screens keep their single compact title and action layout.
    // Only ordinary scrolling pages place the large title in the scroll content.
    val largeTitle = scrollable && !centerTitle && navigation == null && onBack != null
    MinisBackdrop(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                val titleSlot: @Composable () -> Unit = {
                    if (!largeTitle) {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                }
                val navigationSlot: @Composable () -> Unit = {
                    when {
                        navigation != null -> navigation()
                        onBack != null -> IconButton(
                            onClick = onBack,
                            modifier = Modifier.padding(start = 8.dp).size(48.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                if (centerTitle) {
                    CenterAlignedTopAppBar(
                        title = titleSlot,
                        navigationIcon = navigationSlot,
                        actions = { actions?.invoke() },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    )
                } else {
                    TopAppBar(
                        title = titleSlot,
                        navigationIcon = navigationSlot,
                        actions = { actions?.invoke() },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    )
                }
            },
            floatingActionButton = { floatingActionButton?.invoke() },
            bottomBar = { bottomBar?.invoke() },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) { padding ->
            // Preserve the IME-aware viewport for bringIntoViewOnFocus and leave
            // non-scrollable screens in charge of their own LazyColumn/scroll state.
            val baseMod = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
            Column(
                modifier = if (scrollable) baseMod.verticalScroll(rememberScrollState()) else baseMod,
            ) {
                if (largeTitle) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(
                                horizontal = SectionDesign.HeadingHorizontalPadding,
                                vertical = 4.dp,
                            )
                            .semantics { heading() },
                    )
                }
                content()
            }
        }
    }
}

// ─── Switch ────────────────────────────────────────────────────────────────────

/**
 * Compact switch for a settings row whose full width is already a touch target.
 * Waive Material's extra 48dp measurement so the control does not inflate its
 * parent; the clickable row still provides a minimum 52dp target and grows with
 * its text. Standalone switches must retain Material's normal touch target.
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
        SectionCard(content = content)
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
    // Single- and two-line rows share the content-driven settings rhythm.
    minHeight: Dp = if (subtitle == null) SectionDesign.RowMinHeight else SectionDesign.TwoLineRowMinHeight,
    // Long-press on a row that is also tappable (e.g. a skill's context menu).
    // Null keeps the plain clickable, so no other row changes.
    onLongClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // A minimum, never a fixed height: large text remains fully visible.
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
                        .size(SectionDesign.RowIconSize)
                        .background(iconColor.copy(alpha = if (ChatColors.isDark) 0.10f else 0.06f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(SectionDesign.RowIconTextGap))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = SectionDesign.rowTitleStyle(),
                    color = titleColor,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = SectionDesign.rowSubtitleStyle(),
                        color = ChatColors.tertiaryText,
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
                    tint = ChatColors.tertiaryText,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (showDivider) {
            val insetStart = if (icon != null) {
                SectionDesign.RowHorizontalPadding + SectionDesign.RowIconSize + SectionDesign.RowIconTextGap
            } else {
                SectionDesign.DividerStartInset
            }
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
                style = SectionDesign.rowSubtitleStyle(),
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
                .heightIn(min = SectionDesign.RowMinHeight)
                .clickable(onClick = onSelect)
                .padding(horizontal = SectionDesign.RowHorizontalPadding, vertical = SectionDesign.RowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Text(
                title,
                style = SectionDesign.rowTitleStyle(),
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
            .padding(horizontal = SectionDesign.RowHorizontalPadding, vertical = SectionDesign.RowVerticalPadding)
            .fillMaxWidth(),
        content = content,
    )
}
