package com.openminis.app.ui.chat

import com.openminis.app.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.Color

// [T-android-split-chat] Pure tool-label / duration / timestamp formatting
// helpers extracted verbatim from ChatScreen.kt. `internal` so the rest of the
// chat package (still in ChatScreen.kt) can call them across the file boundary.
// No logic change — code moved as-is.

internal val stepTimestampFormatter: java.text.SimpleDateFormat =
    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)

internal fun formatStepTimestamp(epochMs: Long): String =
    stepTimestampFormatter.format(java.util.Date(epochMs))

// [T-step-timestamp v2 aa8b1128] Short "elapsed-or-final duration"
// label for the tool detail header. Cross-platform format contract:
//   < 60s   → "3s"
//   < 1h    → "2m30s" (drops the seconds suffix when seconds == 0)
//   ≥ 1h    → "1h12m"
// When `stillRunning` is true the result is suffixed "…" so the
// header reads "12s…" while the tool is in flight.
// Negative / non-positive values clamp to 0.
internal fun formatStepDuration(seconds: Long, stillRunning: Boolean): String {
    val safe = seconds.coerceAtLeast(0L)
    val base = when {
        safe < 60L -> "${safe}s"
        safe < 3600L -> {
            val m = safe / 60L
            val s = safe % 60L
            if (s == 0L) "${m}m" else "${m}m${s}s"
        }
        else -> {
            val h = safe / 3600L
            val m = (safe % 3600L) / 60L
            if (m == 0L) "${h}h" else "${h}h${m}m"
        }
    }
    return if (stillRunning) "$base…" else base
}

// Static mid-tone accents stay muted on both mist and slate surfaces. Keep
// this non-composable: notification overlays also use the same identity map.
internal fun toolAccentColor(toolName: String): Color = Color(toolAccentColorInt(toolName))

// Helper: tool icon (iOS: distinct SF Symbols per tool type)
internal fun toolIconFor(toolName: String) = when (toolName) {
    "shell_execute" -> Icons.Default.Terminal
    "file_read" -> Icons.Default.Description         // iOS: doc.text
    "file_write" -> Icons.AutoMirrored.Filled.NoteAdd   // iOS: doc.text.fill (filled variant)
    "file_edit" -> Icons.Default.EditNote             // iOS: square.and.pencil
    "browser_use" -> Icons.Default.Language            // iOS: globe
    "read_image" -> Icons.Default.Image                // iOS: photo
    "memory_write", "memory_get" -> Icons.Default.Psychology // iOS: brain.head.profile
    "web_search" -> Icons.Default.Search               // iOS: magnifyingglass
    "subagent_task", "delegate_task", "agent_status" -> Icons.Default.Groups  // iOS: person.2.wave.2
    else -> Icons.Default.Build
}

// [T-android-overlay-two-row] View-layer twin of [toolIconFor], for callers
// that build a classic View hierarchy instead of Compose (the floating-window
// capsule in ToolOverlayController). An ImageVector cannot be handed to an
// ImageView, so the same vocabulary has to exist a second time as drawable
// resources — but it lives HERE, beside the Compose map, so the two are edited
// together and cannot silently diverge. Adding a tool means adding it to both.
@androidx.annotation.DrawableRes
internal fun toolIconResFor(toolName: String?): Int = when (toolName) {
    "shell_execute" -> R.drawable.ic_tool_terminal
    "file_read" -> R.drawable.ic_tool_description
    "file_write" -> R.drawable.ic_tool_note_add
    "file_edit" -> R.drawable.ic_tool_edit_note
    "browser_use" -> R.drawable.ic_tool_globe
    "read_image" -> R.drawable.ic_tool_image
    "memory_write", "memory_get" -> R.drawable.ic_tool_psychology
    "web_search" -> R.drawable.ic_tool_search
    "subagent_task", "delegate_task", "agent_status" -> R.drawable.ic_tool_groups
    else -> R.drawable.ic_tool_build
}

// [T-android-overlay-two-row] Single identity palette for Compose and View
// overlays. Mid-tone blues, sage, amber, and dusk replace vivid system colors.
@androidx.annotation.ColorInt
internal fun toolAccentColorInt(toolName: String?): Int = when (toolName) {
    "shell_execute" -> 0xFF648972.toInt()
    "file_read" -> 0xFF658895.toInt()
    "file_write" -> 0xFF6B80B3.toInt()
    "file_edit" -> 0xFFA07C47.toInt()
    "browser_use" -> 0xFF6B80B3.toInt()
    "read_image" -> 0xFF8A79A0.toInt()
    "memory_write", "memory_get" -> 0xFF9B768C.toInt()
    "web_search" -> 0xFF658895.toInt()
    // Current name and historical names retain the same dusk identity.
    "subagent_task", "delegate_task", "agent_status" -> 0xFF8976A5.toInt()
    else -> 0xFF7B818C.toInt()
}

/**
 * [T-android-overlay-two-row] Humanized fallback title for a tool call whose
 * model-supplied `tool_title` has not arrived (or was never sent).
 *
 * This mirrors ChatViewModel.friendlyToolTitle, which is what the chat stream's
 * tool pill shows in the same situation. Kept as a separate copy rather than
 * hoisting that private member, because ChatViewModel is a very large file that
 * several sessions edit concurrently; the vocabulary here is small and stable.
 */
internal fun friendlyToolTitleFor(toolName: String?): String = when (toolName) {
    null, "" -> "Minis"
    "shell_execute" -> "Execute Shell"
    "file_read" -> "Read File"
    "file_write" -> "Write File"
    "file_edit" -> "Edit File"
    "browser_use" -> "Browse Web"
    "read_image" -> "Read Image"
    "memory_write" -> "Write Memory"
    "memory_get" -> "Read Memory"
    "web_search" -> "Search Web"
    else -> toolName
        .split('_')
        .filter { it.isNotEmpty() }
        .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
}

// Helper: tool display name for "Minis is using X"
internal fun toolDisplayName(toolName: String): String = when (toolName) {
    "shell_execute" -> "terminal"
    "file_read" -> "file reader"
    "file_write" -> "file writer"
    "file_edit" -> "file editor"
    "browser_use" -> "browser"
    "read_image" -> "image viewer"
    "memory_write" -> "memory"
    "memory_get" -> "memory"
    "web_search" -> "search"
    "subagent_task", "delegate_task", "agent_status" -> "agent"
    else -> toolName
}

/**
 * Full "Minis is …" label shown in the tool detail sheet's bottom bar.
 * Mirrors iOS ToolLiveSheet.toolTitle so the wording matches per tool.
 */
internal fun toolTitleLabel(toolName: String): String = when (toolName) {
    "shell_execute" -> "Minis is using Shell"
    "file_read" -> "Minis is reading File"
    "file_write" -> "Minis is using Editor"
    "file_edit" -> "Minis is editing File"
    "browser_use" -> "Minis is using Browser"
    "read_image" -> "Minis is reading Image"
    "memory_write", "memory_get" -> "Minis is using Memory"
    "web_search" -> "Minis is using Search"
    "subagent_task", "delegate_task", "agent_status" -> "Minis is using an Agent"
    else -> "Minis is using ${toolDisplayName(toolName)}"
}

// Helper: format duration (iOS: < 1s → "0.1s", < 60s → "45s", >= 60s → "2m 10s")
internal fun formatToolDuration(ms: Long): String {
    val seconds = ms / 1000.0
    return when {
        seconds < 1 -> String.format("%.1fs", seconds)
        seconds < 60 -> String.format("%.0fs", seconds)
        else -> {
            val m = (seconds / 60).toInt()
            val s = (seconds % 60).toInt()
            "${m}m ${s}s"
        }
    }
}
