package com.openminis.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.VpnLock
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.openminis.app.BuildConfig
import com.openminis.app.R
import com.openminis.app.ui.components.openExternalUrl
import com.openminis.app.ui.components.SectionDesign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProvidersClick: () -> Unit,
    onModelGroupsClick: () -> Unit,
    onRootfsClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onEnvVarsClick: () -> Unit = {},
    /** [T-net-android-phase2] Settings › Network › Network Routing. */
    onNetworkClick: () -> Unit = {},
    /** [FEAT-261021-04] The other rows of the Network section. */
    onTailscaleClick: () -> Unit = {},
    onProxiesClick: () -> Unit = {},
    onContainerDnsClick: () -> Unit = {},
    onSkillsClick: () -> Unit = {},
    onTerminalClick: () -> Unit = {},
    onMemoryClick: () -> Unit = {},
    /** [T-p2-agent-settings] Settings › Agents (delegation on/off), below Memory. */
    onAgentsClick: () -> Unit = {},
    onAgentToolsClick: () -> Unit = {},
    // [T-mcp-integration-android] MCP Integrations page, listed directly below
    // Memory. Default no-op for callers that haven't wired the route yet.
    onMcpClick: () -> Unit = {},
    // [T-soul-md] Soul settings page lives between Skills and Memory in the
    // Agent Runtime section; default no-op for callers that haven't wired
    // the route yet.
    onSoulClick: () -> Unit = {},
    onPermissionsClick: () -> Unit = {},
    onUsageClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onLogsClick: () -> Unit = {},
    // T219-2: Mount External Folders entry. Default no-op for any caller
    // that hasn't wired the route yet.
    onMountedFoldersClick: () -> Unit = {},
    // T235: Shared Folders entry (Shared / Skills / Memory). Default no-op
    // for back-compat with callers wired before T235.
    onSharedFoldersClick: () -> Unit = {},
    // T50: Background & Notifications screen (battery optimisation +
    // OEM autostart guidance). Default no-op so older callers/tests
    // don't need to be retrofitted.
    onBackgroundClick: () -> Unit = {},
    // About retains its routed update, version, and app information screen.
    onAboutClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var showFeedbackSheet by remember { mutableStateOf(false) }
    SettingsScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
    ) {
            // -- LLM Providers --
            SettingsHubSection(
                title = stringResource(R.string.settings_section_llm_providers),
                footer = stringResource(R.string.settings_section_llm_providers_footer),
            ) {
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_manage_providers),
                    subtitle = stringResource(R.string.settings_manage_providers_subtitle),
                    onClick = onProvidersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_model_groups),
                    subtitle = stringResource(R.string.settings_model_groups_subtitle),
                    onClick = onModelGroupsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.BarChart,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_token_usage),
                    subtitle = stringResource(R.string.settings_token_usage_subtitle),
                    onClick = onUsageClick,
                    showDivider = false,
                )
            }

            // -- Appearance --
            SettingsHubSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_section_appearance),
                    subtitle = stringResource(R.string.settings_appearance_subtitle),
                    onClick = onAppearanceClick,
                    showDivider = false,
                )
            }

            // -- Agent Runtime --
            // Capabilities, identity and memory, then connected services.
            SettingsHubSection(title = stringResource(R.string.settings_section_agent_runtime)) {
                // [T-tools-granular-switches] Which optional tools the agent may use.
                SettingsItem(
                    icon = Icons.Outlined.Handyman,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_agent_tools),
                    subtitle = stringResource(R.string.settings_agent_tools_subtitle),
                    onClick = onAgentToolsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Groups,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_agents),
                    subtitle = stringResource(R.string.settings_agents_subtitle),
                    onClick = onAgentsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Extension,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_skills),
                    subtitle = stringResource(R.string.settings_skills_subtitle),
                    onClick = onSkillsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.AutoAwesome,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_soul),
                    subtitle = stringResource(R.string.settings_soul_subtitle),
                    onClick = onSoulClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Psychology,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_memory),
                    subtitle = stringResource(R.string.settings_memory_subtitle),
                    onClick = onMemoryClick,
                )
                // Distinct grid icon for composed MCP services.
                SettingsItem(
                    icon = Icons.Outlined.Dashboard,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_mcp),
                    subtitle = stringResource(R.string.settings_mcp_subtitle),
                    onClick = onMcpClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Terminal,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_env_vars),
                    subtitle = stringResource(R.string.settings_env_vars_subtitle),
                    onClick = onEnvVarsClick,
                    showDivider = false,
                )
            }

            // -- Storage --
            SettingsHubSection(title = stringResource(R.string.settings_section_storage)) {
                SettingsItem(
                    icon = Icons.Outlined.Inventory2,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_section_storage),
                    subtitle = stringResource(R.string.settings_storage_subtitle),
                    onClick = onRootfsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Folder,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_shared_folders),
                    subtitle = stringResource(R.string.settings_shared_folders_subtitle),
                    onClick = onSharedFoldersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.FolderShared,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_mount_external_folders),
                    subtitle = stringResource(R.string.settings_mount_external_folders_subtitle),
                    onClick = onMountedFoldersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Backup,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_backup_restore),
                    subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                    onClick = onBackupClick,
                    showDivider = false,
                )
            }

            // -- Network --
            SettingsHubSection(title = stringResource(R.string.network_title)) {
                SettingsItem(
                    icon = Icons.Outlined.Lan,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.network_routing_title),
                    subtitle = null,
                    onClick = onNetworkClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.VpnLock,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.network_tailscale),
                    subtitle = null,
                    onClick = onTailscaleClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Router,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.network_proxies),
                    subtitle = null,
                    onClick = onProxiesClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Dns,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.network_container_dns),
                    subtitle = null,
                    onClick = onContainerDnsClick,
                    showDivider = false,
                )
            }

            // -- Permissions --
            SettingsHubSection(title = stringResource(R.string.settings_section_permissions)) {
                SettingsItem(
                    icon = Icons.Outlined.Shield,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_section_permissions),
                    subtitle = stringResource(R.string.settings_permissions_subtitle),
                    onClick = onPermissionsClick,
                    showDivider = false,
                )
            }

            // -- Background & Notifications (T50) --
            SettingsHubSection(
                title = stringResource(R.string.bg_section_header),
                footer = stringResource(R.string.bg_section_footer),
            ) {
                SettingsItem(
                    icon = Icons.Outlined.BatteryFull,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.bg_section_header),
                    subtitle = stringResource(R.string.bg_section_subtitle),
                    onClick = onBackgroundClick,
                    showDivider = false,
                )
            }

            // -- Logs --
            SettingsHubSection(title = stringResource(R.string.settings_section_logs)) {
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_section_logs),
                    subtitle = stringResource(R.string.settings_logs_subtitle),
                    onClick = onLogsClick,
                    showDivider = false,
                )
            }

            // -- About --
            SettingsHubSection(title = stringResource(R.string.settings_section_about)) {
                // [T-update-channels] The update badge's Settings layer.
                val updateBadge by com.openminis.app.data.UpdateStateStore.hasUpdateBadge.collectAsState()
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_about_minis),
                    subtitle = stringResource(R.string.settings_about_subtitle),
                    onClick = onAboutClick,
                    badge = updateBadge,
                )
                SettingsItem(
                    icon = Icons.Outlined.FrontHand,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = null,
                    onClick = { openExternalUrl(context, "https://openminis.github.io/privacy-policy.html") },
                )
                SettingsItem(
                    icon = Icons.Outlined.Feedback,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_feedback),
                    subtitle = null,
                    onClick = { showFeedbackSheet = true },
                    showDivider = false,
                )
            }

            Spacer(Modifier.height(24.dp))
        }

    if (showFeedbackSheet) {
        ModalBottomSheet(onDismissRequest = { showFeedbackSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                FeedbackSheetItem(
                    icon = Icons.Outlined.BugReport,
                    title = stringResource(R.string.settings_submit_github_issues),
                    onClick = {
                        showFeedbackSheet = false
                        openExternalUrl(context, buildBugReportUrl())
                    },
                )
                FeedbackSheetItem(
                    icon = Icons.AutoMirrored.Outlined.Send,
                    title = stringResource(R.string.settings_feedback_telegram),
                    onClick = {
                        showFeedbackSheet = false
                        openExternalUrl(context, "https://t.me/+2NzhOJuzRyI1YmM1")
                    },
                )
                FeedbackSheetItem(
                    icon = Icons.Outlined.Email,
                    title = stringResource(R.string.settings_feedback_email),
                    onClick = {
                        showFeedbackSheet = false
                        openExternalUrl(context, buildFeedbackMailto())
                    },
                )
            }
        }
    }
}

@Composable
private fun FeedbackSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SectionDesign.RowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = SectionDesign.ScreenHorizontalPadding, vertical = SectionDesign.RowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = SectionDesign.rowTitleStyle(),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Build the GitHub Issues "new bug report" URL with the body pre-filled
 * from the existing bug-report template. Platform / OS version / app
 * version / device model are injected so the report arrives ready to
 * triage instead of asking the user to fill in environment details.
 *
 * URL shape:
 *   https://github.com/OpenMinis/OpenMinis/issues/new
 *     ?template=bug_report.md
 *     &title=[Bug]
 *     &body=<percent-encoded markdown>
 *
 * The body is a Markdown template with sections for Problem Summary,
 * Basic Information (table — auto-filled), Steps to Reproduce, Error
 * Details (fenced code block), Expected Behavior, and Additional
 * Information.
 */
private fun buildBugReportUrl(): String {
    val osVersion = android.os.Build.VERSION.RELEASE
    val sdkInt = android.os.Build.VERSION.SDK_INT
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE
    val manufacturer = android.os.Build.MANUFACTURER
    val model = android.os.Build.MODEL

    // Body matches the spec template. Triple-backtick fences are written
    // as "```" — they survive percent-encoding cleanly. Indentation here
    // is significant: trimIndent() removes the common Kotlin indentation
    // but preserves the Markdown structure as-is.
    val body = """
        ## 📝 Problem Summary

        <!-- Briefly describe the issue you encountered -->


        ## 📱 Basic Information

        | Field | Value |
        |-------|-------|
        | Platform | Android |
        | OS Version | Android $osVersion (API $sdkInt) |
        | Minis Version | $versionName (build $versionCode) |
        | Device Model | $manufacturer $model |

        ## 🔁 Steps to Reproduce

        1.
        2.
        3.

        ## ❌ Error Details

        ```
        paste error here
        ```

        ## ✅ Expected Behavior



        ## 🗂️ Additional Information

    """.trimIndent()

    val encodedBody = java.net.URLEncoder.encode(body, "UTF-8")
    // Title carries a "[Bug] " prefix with a trailing space so the cursor
    // lands after it on GitHub's page; encode the space as %20 explicitly
    // since URLEncoder turns spaces into '+' which GitHub also accepts but
    // the spec calls for the literal "[Bug] " form.
    val title = java.net.URLEncoder.encode("[Bug] ", "UTF-8")
    return "https://github.com/OpenMinis/OpenMinis/issues/new" +
        "?template=bug_report.md" +
        "&title=$title" +
        "&body=$encodedBody"
}

/** Feedback email includes the installed version and device information. */
private fun buildFeedbackMailto(): String {
    val body = """
        Please describe your feedback:


        ---
        App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Android Version: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})
        Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}

        Screenshot (optional): Please attach a screenshot if relevant.
    """.trimIndent()
    val subject = java.net.URLEncoder.encode("Minis Feedback", "UTF-8")
    val encodedBody = java.net.URLEncoder.encode(body, "UTF-8")
    return "mailto:dev@openminis.app?subject=$subject&body=$encodedBody"
}

/** The hub uses the same grouping primitive as all settings detail pages. */
@Composable
private fun SettingsHubSection(
    title: String,
    footer: String? = null,
    content: @Composable () -> Unit,
) {
    SettingsSection(header = title, footer = footer) { content() }
}

/** Shared row styling with the existing update indicator kept in the trailing slot. */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    // [T-update-channels] A small dot before the chevron.
    badge: Boolean = false,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconColor = iconColor,
        onClick = onClick,
        showDivider = showDivider,
        trailing = if (badge) ({ UpdateBadgeDot() }) else null,
    )
}

