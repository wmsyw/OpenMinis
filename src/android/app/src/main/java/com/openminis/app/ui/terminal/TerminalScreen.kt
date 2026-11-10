package com.openminis.app.ui.terminal

import com.openminis.app.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eject
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.openminis.app.sandbox.TerminalSession
import com.openminis.app.terminal.MinisOpenUrlBroker
import com.openminis.app.ui.terminal.canvas.TerminalNativeViewCompose
import com.openminis.app.ui.terminal.canvas.TerminalInputView
import com.openminis.app.ui.terminal.canvas.rememberTerminalInputController
import com.openminis.app.ui.terminal.emulator.TerminalEmulator
import kotlinx.coroutines.launch

// The terminal renderer owns its ANSI palette; only the surrounding chrome
// follows the app's mist / slate surfaces.
private val TerminalBg = Color.Black

@Composable
fun TerminalScreen(
    terminalSession: TerminalSession,
    onBack: () -> Unit,
    initCommand: String? = null,
    /**
     * When non-null, binds this terminal to the given chat session —
     * TerminalSession.start() will chdir into /var/minis and pick up the
     * session's env vars (mirrors iOS "Open Terminal" from chat).
     */
    sessionId: String? = null,
) {
    val emulator = remember { TerminalEmulator() }
    val context = LocalContext.current
    val inputController = rememberTerminalInputController()
    val scope = rememberCoroutineScope()
    var ctrlActive by remember { mutableStateOf(false) }

    // Pipe PTY output → emulator.
    LaunchedEffect(terminalSession) {
        terminalSession.outputBytes.collect { bytes ->
            emulator.feed(bytes)
        }
    }

    // Wire emulator responses (DSR etc.) back to the PTY.
    DisposableEffect(terminalSession, emulator) {
        emulator.onResponse = { data -> terminalSession.sendRawBytes(data) }
        onDispose { emulator.onResponse = null }
    }

    // Clear emulator when session clearOutput() ticks.
    val clearVersion by terminalSession.clearVersion.collectAsStateEffect()
    LaunchedEffect(clearVersion) {
        if (clearVersion > 0) {
            emulator.feed("\u001Bc".toByteArray())   // RIS — full reset
        }
    }

    // Start session + optional initCommand.
    LaunchedEffect(Unit) {
        if (!terminalSession.isRunning) terminalSession.start(sessionId = sessionId)
        if (!initCommand.isNullOrBlank()) {
            // Pre-fill at the prompt without newline so the user can review.
            kotlinx.coroutines.delay(500)
            terminalSession.sendText(initCommand)
        }
    }

    // T-android-terminal-keyboard-3a8f5e0b: Do NOT auto-focus the hidden input
    // EditText on open. Previously a 20-tick retry loop here force-popped the
    // IME on entry (matching iOS becomeFirstResponder), but on Android that
    // caused two issues: (1) keyboard pops up unsolicited when the user just
    // wants to read terminal output, (2) the back gesture's first press hides
    // the IME, then the still-living retry loop (or a focus-restore on next
    // composition) re-pops it, requiring a second back press to actually
    // leave the screen. Android convention for read-eval shells is to let the
    // user tap the canvas (handled below via `onTap = { requestFocus() }`) or
    // the keyboard-toggle button in the accessory bar to deliberately invoke
    // the IME — that single user action handles both opening the keyboard
    // and (via the toggle) closing it, and a single back press now exits.

    // [T-android-terminal-enter-keeps-focus] ...but DO auto-focus when a
    // hardware keyboard is attached.
    //
    // Every objection in the note above is about the soft IME: it pops up
    // unsolicited over the output, and it fights the back gesture. With a
    // physical keyboard there is no IME to pop — focus is invisible — so the
    // only thing auto-focus changes is that the keys the user presses arrive
    // somewhere. Without it, opening the terminal and typing does nothing at
    // all, with no on-screen hint as to why.
    // [T-android-hwkeyboard-virtual-false-positive] Same rule as the chat
    // composer — see HardwareKeyboard. Configuration.keyboard alone also
    // counts virtual HID keyboards (scrcpy --uhid), which would auto-focus the
    // terminal input on a touch-only phone.
    val hasHardwareKeyboard = com.openminis.app.ui.components.rememberHasHardwareKeyboard()
    LaunchedEffect(hasHardwareKeyboard) {
        if (hasHardwareKeyboard) inputController.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose { terminalSession.stop() }
    }

    // Claim the broker while the fullscreen terminal is up so ChatScreen
    // (still composed underneath this destination's stack) doesn't try to
    // present its own preview sheet on top — mirrors iOS ISHTerminalView.
    DisposableEffect(Unit) {
        MinisOpenUrlBroker.setTerminalVisible(true)
        onDispose { MinisOpenUrlBroker.setTerminalVisible(false) }
    }

    // OSC 1337 MinisOpenURL emitted by `/usr/local/bin/minis-open` is parsed
    // by TerminalEmulator and forwarded to MinisOpenUrlBroker. From the
    // standalone terminal we only route web schemes (http(s)/about) into an
    // in-app WebView preview; minis://-style chat resources need ChatScreen's
    // resolver and aren't reachable here, so we still consume them to avoid
    // leaking a stale pendingUrl back to chat on next attach.
    var previewUrl by remember { mutableStateOf<String?>(null) }
    val pendingUrl by MinisOpenUrlBroker.pendingUrl.collectAsStateEffect()
    LaunchedEffect(pendingUrl) {
        val uri = pendingUrl ?: return@LaunchedEffect
        if (MinisOpenUrlBroker.isWebScheme(uri.scheme)) {
            previewUrl = uri.toString()
        }
        MinisOpenUrlBroker.consume()
    }

    // T290: Layered layout — top bar fixed, canvas fills middle, accessory
    // bar pinned to bottom and lifted above the IME via Modifier.imePadding().
    //
    // Pre-T290 this was an off-window Popup + manually-tracked IME height
    // built around a Pixel/Gboard PAN-mode quirk. That misfired on pixel6
    // (bar still under the keyboard). With windowSoftInputMode=adjustResize
    // already set in the manifest, edge-to-edge enabled, and modern Pixel
    // builds reporting WindowInsets.ime correctly, a plain in-window Box +
    // imePadding does the right thing without any custom tracking.
    val density = LocalDensity.current
    var accessoryBarHeightDp by remember { mutableStateOf(64.dp) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Main content: top bar + canvas. imePadding() lifts the canvas
        // above the keyboard so it's never covered.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .padding(bottom = accessoryBarHeightDp),
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TerminalBg),
            ) {
                // T194 part-2: native Android View backing gives us long-press
                // selection + ActionMode + ClipboardManager copy. The old
                // TerminalCanvasView is left in the package as a Compose-only
                // fallback if anything regresses with the View interop path.
                TerminalNativeViewCompose(
                    emulator = emulator,
                    onResize = { cols, rows ->
                        emulator.resize(cols, rows)
                        terminalSession.setWindowSize(cols, rows)
                    },
                    onTap = { inputController.requestFocus() },
                )
                TerminalInputView(
                    onInput = { bytes ->
                        // Any user input snaps back to live tail so typing is visible.
                        emulator.scrollOffset = 0
                        if (ctrlActive && bytes.size == 1) {
                            val ch = bytes[0].toInt().toChar().uppercaseChar()
                            if (ch in 'A'..'Z') {
                                terminalSession.sendRawBytes(byteArrayOf((ch - 'A' + 1).toByte()))
                                ctrlActive = false
                                return@TerminalInputView
                            }
                        }
                        terminalSession.sendRawBytes(bytes)
                    },
                    applicationCursorKeys = emulator.applicationCursorKeys,
                    controller = inputController,
                    modifier = Modifier.size(1.dp),
                )
            }
        }

        // Top bar pinned at top — never moves with IME.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.systemBars)
                .height(64.dp)
                .background(MaterialTheme.colorScheme.background),
        ) {
            TerminalTopBar(
                onClose = {
                    terminalSession.stop()
                    onBack()
                },
                onClear = {
                    // T310: send Ctrl+U (NAK, 0x15) so readline kills any
                    // half-typed line in the shell. Otherwise those chars
                    // stay in the line buffer and get prepended to the
                    // user's next command after the visual clear.
                    terminalSession.sendRawBytes(byteArrayOf(0x15))
                    terminalSession.clearOutput()
                    emulator.feed("\u001Bc".toByteArray())
                },
            )
        }

        // T290: accessory bar — anchored to bottom of the parent Box and
        // lifted above the IME via imePadding(). When the keyboard is
        // closed, windowInsetsPadding(navigationBars) keeps it above the
        // gesture / nav bar. No Popup, no manual IME tracking.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .onSizeChanged { size ->
                    accessoryBarHeightDp = with(density) { size.height.toDp() }
                },
        ) {
            KeyboardAccessoryBar(
                ctrlActive = ctrlActive,
                keyboardVisible = inputController.isFocused,
                onCtrlToggle = { ctrlActive = !ctrlActive },
                onToggleKeyboard = {
                    if (inputController.isFocused) inputController.clearFocus()
                    else inputController.requestFocus()
                },
                onCopyAll = { copyAllToClipboard(context, emulator) },
                onSendRaw = { bytes ->
                    emulator.scrollOffset = 0
                    terminalSession.sendRawBytes(bytes)
                },
                onArrow = { dir ->
                    emulator.scrollOffset = 0
                    val prefix = if (emulator.applicationCursorKeys)
                        byteArrayOf(0x1B, 'O'.code.toByte())
                    else byteArrayOf(0x1B, '['.code.toByte())
                    terminalSession.sendRawBytes(prefix + byteArrayOf(dir.code.toByte()))
                },
            )
        }

        previewUrl?.let { url ->
            com.openminis.app.ui.components.UrlPreviewSheet(
                url = url,
                onDismiss = { previewUrl = null },
            )
        }
    }
}

// ── Tiny helper: collectAsState for StateFlow<Int> without pulling all of androidx.lifecycle ──
@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateEffect(): androidx.compose.runtime.State<T> {
    val state = remember { androidx.compose.runtime.mutableStateOf(value) }
    LaunchedEffect(this) { collect { state.value = it } }
    return state
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun TerminalTopBar(
    onClose: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularIconButton(
            icon = Icons.Default.Close,
            contentDescription = stringResource(R.string.common_close),
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onClose,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.terminal_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.weight(1f))
        CircularIconButton(
            icon = Icons.Default.Brush,
            contentDescription = stringResource(R.string.terminal_clear),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onClear,
        )
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─── Copy All (#451) ──────────────────────────────────────────────────────────

/**
 * [#451] The terminal's whole output to the clipboard, with a Toast. Runs on
 * the main thread, which is also where the PTY output is fed, so the buffer
 * is not changing underneath. A clip goes through Binder (about 1 MB a
 * transaction), so a very wide, CJK-heavy history can be refused: that is
 * reported instead of crashing.
 */
private fun copyAllToClipboard(context: Context, emulator: TerminalEmulator) {
    val text = emulator.copyableText()
    if (text.isEmpty()) return
    runCatching {
        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("Minis Shell", text))
    }.onSuccess {
        Toast.makeText(context, R.string.terminal_copied_all, Toast.LENGTH_SHORT).show()
    }.onFailure {
        com.openminis.app.logging.AppLogger.error("TerminalScreen", "copy all (${text.length} chars) failed: $it")
        Toast.makeText(
            context,
            context.getString(R.string.image_copy_failed_toast, it.message ?: it.javaClass.simpleName),
            Toast.LENGTH_SHORT,
        ).show()
    }
}

// ─── Keyboard accessory bar ───────────────────────────────────────────────────

@Composable
private fun KeyboardAccessoryBar(
    ctrlActive: Boolean,
    keyboardVisible: Boolean,
    onCtrlToggle: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onCopyAll: () -> Unit,
    onSendRaw: (ByteArray) -> Unit,
    onArrow: (Char) -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        QuickCommandButton(
            label = stringResource(if (keyboardVisible) R.string.terminal_hide_keyboard else R.string.terminal_show_keyboard),
            icon = if (keyboardVisible) Icons.Default.KeyboardHide else Icons.Outlined.Keyboard,
            onClick = onToggleKeyboard,
        )
        // [#451] Copy the whole output (scrollback + screen). Here rather
        // than in the top bar, where it would sit next to Clear.
        QuickCommandButton(
            label = stringResource(R.string.terminal_copy_all),
            icon = Icons.Outlined.ContentCopy,
            onClick = onCopyAll,
        )
        QuickCommandButton("Esc", iconText = "⎋") { onSendRaw(byteArrayOf(0x1B)) }
        QuickCommandButton("Tab", icon = Icons.AutoMirrored.Filled.KeyboardTab) { onSendRaw(byteArrayOf(0x09)) }
        // [T-android-shell-toolbar-enter-key] The soft keyboard's Return
        // inserts a newline inside the terminal, so it can't send a real
        // carriage return to run a command line / trigger an in-CLI prompt.
        // This writes CR (0x0D) on the same raw-PTY path as Esc/Tab/C-c.
        // Placed right after Tab, mirroring iOS fa3d2f8c.
        QuickCommandButton("⏎", iconText = "⏎") { onSendRaw(byteArrayOf(0x0D)) }
        QuickCommandButton("Ctrl", iconText = "^", isActive = ctrlActive, onClick = onCtrlToggle)
        QuickCommandButton("\u2191", icon = Icons.Default.KeyboardArrowUp) { onArrow('A') }
        QuickCommandButton("\u2193", icon = Icons.Default.KeyboardArrowDown) { onArrow('B') }
        QuickCommandButton("\u2190", icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onArrow('D') }
        QuickCommandButton("\u2192", icon = Icons.AutoMirrored.Filled.KeyboardArrowRight) { onArrow('C') }
        QuickCommandButton("C-c", icon = Icons.Outlined.Cancel) { onSendRaw(byteArrayOf(0x03)) }
        QuickCommandButton("C-d", icon = Icons.Default.Eject) { onSendRaw(byteArrayOf(0x04)) }
        QuickCommandButton("C-z", icon = Icons.Outlined.PauseCircle) { onSendRaw(byteArrayOf(0x1A)) }
        }
    }
}

@Composable
private fun QuickCommandButton(
    label: String,
    icon: ImageVector? = null,
    iconText: String? = null,
    isActive: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when {
            icon != null -> Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            iconText != null -> Text(iconText, color = fg, style = MaterialTheme.typography.labelLarge)
        }
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}
