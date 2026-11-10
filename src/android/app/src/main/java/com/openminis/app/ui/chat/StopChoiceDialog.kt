package com.openminis.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openminis.app.R
import com.openminis.app.ui.theme.ChatColors

/**
 * [T-stop-choice] "What should stop?" - shown instead of an immediate Stop
 * when the conversation has background work a Stop would also end (sub
 * agents, scheduled tasks; [StopSummary.hasBackground]).
 *
 * Lists what is running and how many of each, then offers:
 *  - Stop Current Tool Only ([onToolOnly], the safe primary choice, nearest the
 *    thumb) - only when this chat's own turn is running ([canStopToolOnly]);
 *    with only background work there is no "current tool" to stop;
 *  - Stop All ([onStopAll], destructive);
 *  - Cancel.
 *
 * Also used by New Chat's "a task is running" guard, with its own title and
 * button labels and the [body] override.
 *
 * Uses a neutral, rounded bottom panel with native Material actions. The
 * current-tool action keeps the primary accent; stopping everything uses
 * the error role. The scope labels and callbacks are shared with New Chat's
 * running-task guard.
 */
@Composable
internal fun StopChoiceDialog(
    summary: StopSummary,
    canStopToolOnly: Boolean,
    onDismiss: () -> Unit,
    onToolOnly: () -> Unit,
    onStopAll: () -> Unit,
    title: String = stringResource(R.string.chat_stop_choice_title),
    toolOnlyText: String = stringResource(R.string.chat_stop_choice_tool_only),
    stopAllText: String = stringResource(R.string.chat_stop_choice_all),
    body: String? = null,
) {
    val lines = buildList {
        if (canStopToolOnly) {
            add(
                if (summary.runningTools.isEmpty()) {
                    stringResource(R.string.chat_stop_choice_current_reply)
                } else {
                    stringResource(R.string.chat_stop_choice_current_tools, summary.runningTools.distinct().joinToString(", "))
                },
            )
        }
        if (summary.subAgentsRunning > 0) {
            add(pluralStringResource(R.plurals.chat_stop_choice_subagents, summary.subAgentsRunning, summary.subAgentsRunning))
        }
        if (summary.subAgentsQueued > 0) {
            add(pluralStringResource(R.plurals.chat_stop_choice_subagents_queued, summary.subAgentsQueued, summary.subAgentsQueued))
        }
        if (summary.scheduledTasks > 0) {
            add(pluralStringResource(R.plurals.chat_stop_choice_scheduled, summary.scheduledTasks, summary.scheduledTasks))
        }
    }
    val footer = buildList {
        if (canStopToolOnly) add(stringResource(R.string.chat_stop_choice_tool_only_footer))
        add(stringResource(R.string.chat_stop_choice_all_footer))
    }.joinToString(" ")
    val message = buildString {
        body?.let { append(it).append("\n\n") }
        append(lines.joinToString("\n"))
        if (lines.isNotEmpty()) append("\n\n")
        append(footer)
    }
    StopActionSheet(
        title = title,
        message = message,
        onDismiss = onDismiss,
        actions = buildList {
            if (canStopToolOnly) add(SheetAction(toolOnlyText, destructive = false, onToolOnly))
            add(SheetAction(stopAllText, destructive = true, onStopAll))
        },
    )
}

private class SheetAction(val label: String, val destructive: Boolean, val onClick: () -> Unit)

@Composable
private fun StopActionSheet(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    actions: List<SheetAction>,
) {
    val card = MaterialTheme.colorScheme.surfaceContainerHigh
    val accent = MaterialTheme.colorScheme.primary
    val red = MaterialTheme.colorScheme.error
    val hairline = ChatColors.separator
    val shape = RoundedCornerShape(28.dp)
    val noRipple = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        // Full-window so the panel sits at the bottom; outside taps dismiss it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    // Taps on the sheet itself do not dismiss it.
                    .clickable(interactionSource = noRipple, indication = null, onClick = {}),
            ) {
                // [T-stop-sheet-fits] In landscape or at a large font scale the
                // sheet can be taller than the window. Cancel is measured first
                // (this card is weighted, not filling), and inside the card the
                // buttons come before the title and message, which scroll in
                // what is left - as the platform alert dialog does. Before,
                // the bottom buttons laid out past the window edge.
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(card),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(androidx.compose.foundation.rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = ChatColors.primaryText,
                        )
                        if (message.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ChatColors.secondaryText,
                            )
                        }
                    }
                    for (action in actions) {
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(hairline))
                        SheetButton(action.label, if (action.destructive) red else accent, FontWeight.Normal) {
                            action.onClick()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(shape).background(card)) {
                    SheetButton(stringResource(R.string.cancel), accent, FontWeight.SemiBold, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun SheetButton(label: String, color: Color, weight: FontWeight, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = weight),
            textAlign = TextAlign.Center,
        )
    }
}
