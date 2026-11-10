package com.openminis.app.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.openminis.app.R
import com.openminis.app.data.db.DatabaseVersionGuard
import com.openminis.app.ui.settings.KEY_FONT_APP_BASE
import com.openminis.app.ui.settings.KEY_THEME_MODE
import com.openminis.app.ui.settings.fontScaleForLevel
import com.openminis.app.ui.settings.getAppearancePrefs
import com.openminis.app.ui.theme.MinisTheme

/**
 * [T-android-downgrade-compat] Shown when the chat database on disk was
 * written by a newer build than the one now running.
 *
 * The point of this screen is that the previous behaviour — Room throwing at
 * first database access, so the app simply never opened — was indistinguishable
 * from data loss even though nothing was lost. The database has NOT been
 * touched: the version was read through a read-only handle and Room was never
 * constructed, so no migration and no `dropAllTables` could have run.
 *
 * Deliberately offers no "clear data and continue" button. That is the one
 * action that would actually destroy the user's history, and offering it to
 * someone who is confused and just wants the app to open is how data gets
 * lost. Reinstalling the newer build restores everything.
 */
@Composable
fun NewerDatabaseGuidanceScreen(onExit: () -> Unit) {
    // Appearance is backed by SharedPreferences, so this path never touches
    // the intentionally uninitialized application repositories or database.
    val context = LocalContext.current
    val prefs = remember(context) { getAppearancePrefs(context) }
    var themeMode by remember(prefs) { mutableIntStateOf(prefs.getInt(KEY_THEME_MODE, 0)) }
    var appBaseLevel by remember(prefs) { mutableIntStateOf(prefs.getInt(KEY_FONT_APP_BASE, 0)) }
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            when (key) {
                KEY_THEME_MODE -> themeMode = sp.getInt(KEY_THEME_MODE, 0)
                KEY_FONT_APP_BASE -> appBaseLevel = sp.getInt(KEY_FONT_APP_BASE, 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    MinisTheme(darkTheme = darkTheme, fontScale = fontScaleForLevel(appBaseLevel)) {
        val activity = context as? ComponentActivity
        val background = MaterialTheme.colorScheme.background
        val windowDrawable = remember(background) { ColorDrawable(background.toArgb()) }
        SideEffect {
            activity?.let {
                it.window.setBackgroundDrawable(windowDrawable)
                val barStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                it.enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.newer_db_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.newer_db_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    // Concrete numbers, because "a newer version" alone leaves
                    // the user with nothing to check or report.
                    Text(
                        text = stringResource(
                            R.string.newer_db_versions,
                            DatabaseVersionGuard.CODE_DB_VERSION,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(stringResource(R.string.newer_db_exit))
                    }
                }
            }
        }
    }
}
