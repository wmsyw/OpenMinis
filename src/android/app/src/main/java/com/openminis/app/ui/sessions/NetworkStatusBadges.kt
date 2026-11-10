package com.openminis.app.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.VpnLock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openminis.app.R
import com.openminis.app.network.NetworkAlertCenter
import com.openminis.app.network.NetworkAlertEvent
import com.openminis.app.network.NetworkHub
import com.openminis.app.network.NetworkStatusMonitor
import com.openminis.app.network.NetworkStatusMonitor.ProxyLevel
import com.openminis.app.network.NetworkStatusMonitor.TailscaleLevel
import com.openminis.app.ui.navigation.Routes

/**
 * [FEAT-261021-07, iOS a6e6c10d1] The small pills under the home screen's
 * title: whether Tailscale is connected and whether the proxy in use
 * answers. Renders nothing unless Network Routing is on and Tailscale or a
 * proxy is in use. At most two pills - Tailscale, and one for proxies (a
 * single proxy is named; several share a "Proxies ×N" pill with the worst
 * state). A tap opens the matching page of Settings > Network through
 * [onOpen] (a route; a single proxy's editor is opened on that page).
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun NetworkStatusBadges(onOpen: (String) -> Unit) {
    val settings by NetworkHub.settings.collectAsState()
    if (settings.forceDirect || !NetworkStatusMonitor.wanted(settings)) return
    val ts by NetworkHub.tailscale.collectAsState()
    val applying by NetworkHub.applying.collectAsState()
    val lastError by NetworkHub.lastError.collectAsState()
    val failing by NetworkAlertCenter.failing.collectAsState()
    val health by NetworkStatusMonitor.proxyHealth.collectAsState()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (settings.nodeWanted) {
            val level = NetworkStatusMonitor.tailscaleLevel(
                ts, applying, lastError, NetworkAlertEvent.Subject.Tailscale in failing,
            )
            val (state, color) = tailscaleDisplay(level)
            Pill(Icons.Outlined.VpnLock, "Tailscale", state, color) { onOpen(Routes.NETWORK_TAILSCALE) }
        }
        val proxies = NetworkStatusMonitor.proxiesInUse(settings)
        if (proxies.isNotEmpty()) {
            val worst = proxies.maxOf { NetworkStatusMonitor.proxyLevel(it.id, health, failing.keys) }
            val (state, color) = proxyDisplay(worst)
            val single = proxies.singleOrNull()
            val name = single?.displayName ?: (stringResource(R.string.network_proxies) + " ×${proxies.size}")
            Pill(Icons.Outlined.Router, name, state, color) {
                single?.let { NetworkHub.pendingProxyEditor.value = it.id }
                onOpen(Routes.NETWORK_PROXIES)
            }
        }
    }
}

@Composable
private fun Pill(icon: ImageVector, name: String, state: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$name, $state" },
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 80.dp),
        )
        Text(
            " · $state",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}


@Composable
private fun tailscaleDisplay(level: TailscaleLevel): Pair<String, Color> {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    return when (level) {
        TailscaleLevel.CONNECTING -> stringResource(R.string.network_status_connecting) to secondary
        TailscaleLevel.CONNECTED -> stringResource(R.string.network_status_connected) to MaterialTheme.colorScheme.primary
        TailscaleLevel.UNAVAILABLE -> stringResource(R.string.network_badge_unavailable) to MaterialTheme.colorScheme.error
        TailscaleLevel.NOT_LOGGED_IN -> stringResource(R.string.network_status_not_logged_in) to MaterialTheme.colorScheme.onSurfaceVariant
        TailscaleLevel.AWAITING_APPROVAL -> stringResource(R.string.network_badge_awaiting_approval) to MaterialTheme.colorScheme.onSurfaceVariant
        TailscaleLevel.NOT_CONNECTED -> stringResource(R.string.network_badge_not_connected) to MaterialTheme.colorScheme.error
    }
}

@Composable
private fun proxyDisplay(level: ProxyLevel): Pair<String, Color> = when (level) {
    ProxyLevel.AVAILABLE -> stringResource(R.string.network_badge_available) to MaterialTheme.colorScheme.primary
    ProxyLevel.CHECKING -> stringResource(R.string.network_diagnostics_running) to MaterialTheme.colorScheme.onSurfaceVariant
    ProxyLevel.UNAVAILABLE -> stringResource(R.string.network_badge_unavailable) to MaterialTheme.colorScheme.error
}
