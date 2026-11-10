package com.openminis.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * App-wide popup menu with 14dp corners and a clean, neutral panel.
 *
 * Drop-in replacement for [androidx.compose.material3.DropdownMenu] — pass
 * [DropdownMenuItem] children as before.
 */
@Composable
fun MinisMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation: Dp = 0.dp,
    // Android's popup window reserves at most 8dp for a shadow.
    shadowElevation: Dp = 3.dp,
    border: BorderStroke? = null,
    minWidth: Dp = 180.dp,
    // Optional physical right-edge anchoring, with automatic vertical flipping.
    alignEnd: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Share positioning and anchor-based entry across both alignment modes.
    // Dismissal remains immediate so no invisible popup can intercept input.
    if (!expanded) return
    val density = LocalDensity.current
    val offsetXPx = with(density) { offset.x.roundToPx() }
    val offsetYPx = with(density) { offset.y.roundToPx() }
    val marginXPx = with(density) { MinisMenuPosition.HORIZONTAL_MARGIN.roundToPx() }
    val marginYPx = with(density) { MinisMenuPosition.VERTICAL_MARGIN.roundToPx() }
    Popup(
        onDismissRequest = onDismissRequest,
        properties = properties,
        popupPositionProvider = remember(offsetXPx, offsetYPx, alignEnd, marginXPx, marginYPx) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset = MinisMenuPosition.compute(
                    anchorBounds, windowSize, popupContentSize,
                    alignEnd, offsetXPx, offsetYPx, marginXPx, marginYPx,
                )
            }
        },
    ) {
        // [T-android-menu-screen-margin] The popup's constraints are the
        // window; cap the menu at the window height minus both vertical
        // margins so a long menu still fits inside the margins and scrolls
        // (its Column is verticalScroll) instead of being pushed off-screen.
        BoxWithConstraints {
            val maxMenuHeight = (maxHeight - MinisMenuPosition.VERTICAL_MARGIN * 2).coerceAtLeast(0.dp)
            // Scale and fade from the anchor corner on entry.
            // Seeded false then flipped true on first composition so the enter
            // animation runs as the Popup mounts. transformOrigin is the anchor
            // corner: top-right for alignEnd (right-anchored), top-left otherwise.
            val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
            val origin = if (alignEnd) {
                androidx.compose.ui.graphics.TransformOrigin(1f, 0f)
            } else {
                androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
            }
            AnimatedVisibility(
                visibleState = visibleState,
                enter = scaleIn(
                    animationSpec = tween(200),
                    initialScale = 0.85f,
                    transformOrigin = origin,
                ) + fadeIn(animationSpec = tween(200)),
                exit = scaleOut(animationSpec = tween(120)) + fadeOut(animationSpec = tween(120)),
            ) {
                Surface(
                    // [T-android-minis-menu-width] Size to the CONTENT's intrinsic
                    // width (the widest menu item), then clamp to [minWidth, 280dp].
                    // This restores Material3 DropdownMenu's "hug the content" sizing
                    // that the unified-Popup rewrite lost: without IntrinsicSize.Max,
                    // DropdownMenuItem's internal fillMaxWidth expanded the Column to
                    // the 280dp cap, making short menus (the top-bar tools menu) look
                    // far too wide (T252's "blows out" symptom). 280dp stays as the
                    // upper bound so a very long localised item can't span the screen.
                    modifier = modifier
                        .width(IntrinsicSize.Max)
                        .widthIn(min = minWidth, max = 280.dp)
                        .heightIn(max = maxMenuHeight),
                    shape = shape,
                    color = containerColor,
                    tonalElevation = tonalElevation,
                    // Never past the Popup window's 8dp shadow reserve (see the
                    // shadowElevation parameter): anything larger is clipped.
                    shadowElevation = shadowElevation.coerceAtMost(MinisMenuPosition.MAX_POPUP_SHADOW_ELEVATION),
                    border = border,
                ) {
                    // Mirrors DropdownMenu's content layout: a vertically-scrollable
                    // Column inside the surface (scrollState preserved from the old
                    // DropdownMenu branch so long menus still scroll).
                    Column(
                        modifier = Modifier.verticalScroll(scrollState),
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * [T-android-menu-screen-margin] Where a [MinisMenu] goes — a pure function so
 * the edge cases are unit-tested (MinisMenuPositionTest).
 *
 * Semantics kept from the original provider: default = popup's left edge on
 * the anchor's left edge, [alignEnd] = popup's right edge on the anchor's
 * right edge; [offsetX]/[offsetY] shift it (callers rely on this: finger-
 * anchored session menus, the 6dp gap under message bubbles); below the
 * anchor, flipped above when it does not fit below and does fit above.
 *
 * What changed: the bounds are the window inset by a margin, not the window
 * itself. The old clamp to [0, window - popup] pushed an overflowing menu flush
 * against the screen edge (measured 2px on a Pixel 4a). Material3's
 * DropdownMenuPositionProvider keeps an 8dp horizontal margin the same way.
 * When the popup is larger than the space between the margins it is centred,
 * so the clamp range is never inverted (min > max would throw).
 */
internal object MinisMenuPosition {
    /** Same as Material3's MenuHorizontalMargin. */
    val HORIZONTAL_MARGIN: Dp = 8.dp

    /**
     * Smaller than Material3's 48dp MenuVerticalMargin on purpose: some of our
     * menus are tall (session long-press, top-bar tools), and 48dp top and
     * bottom would force them to scroll much sooner. The popup window already
     * excludes the status and navigation bars, so 8dp is only breathing room.
     */
    val VERTICAL_MARGIN: Dp = 8.dp

    /** AndroidPopup's `maxSupportedElevation`: the shadow room a Popup window reserves. */
    val MAX_POPUP_SHADOW_ELEVATION: Dp = 8.dp

    fun compute(
        anchorBounds: IntRect,
        windowSize: IntSize,
        popupSize: IntSize,
        alignEnd: Boolean,
        offsetX: Int,
        offsetY: Int,
        marginX: Int,
        marginY: Int,
    ): IntOffset {
        val rawX = if (alignEnd) anchorBounds.right - popupSize.width + offsetX else anchorBounds.left + offsetX
        val x = clampOrCenter(rawX, popupSize.width, windowSize.width, marginX)
        // Below the anchor; flip above when it does not fit below and does fit
        // above (within the margins). Neither fits: stay below and let the
        // clamp slide it up.
        val belowY = anchorBounds.bottom + offsetY
        val aboveY = anchorBounds.top - popupSize.height - offsetY
        val fitsBelow = belowY + popupSize.height <= windowSize.height - marginY
        val y = if (fitsBelow || aboveY < marginY) belowY else aboveY
        return IntOffset(x, clampOrCenter(y, popupSize.height, windowSize.height, marginY))
    }

    /** [value] clamped into [margin, window - margin - size]; centred when [size] does not fit. */
    internal fun clampOrCenter(value: Int, size: Int, window: Int, margin: Int): Int {
        val max = window - margin - size
        return if (max < margin) (window - size) / 2 else value.coerceIn(margin, max)
    }
}

/**
 * Subtle inset divider using the same neutral rule as chat and settings.
 */
@Composable
fun MinisMenuDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * Shared padding and semantic colors for native Material menu items.
 * The native 48dp minimum target remains intact.
 */
object MinisMenuDefaults {
    val ItemPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 0.dp)

    @Composable
    fun itemColors(
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        leadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        trailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): MenuItemColors = MenuDefaults.itemColors(
        textColor = textColor,
        leadingIconColor = leadingIconColor,
        trailingIconColor = trailingIconColor,
    )

    @Composable
    fun destructiveItemColors(): MenuItemColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.error,
        leadingIconColor = MaterialTheme.colorScheme.error,
        trailingIconColor = MaterialTheme.colorScheme.error,
    )
}
