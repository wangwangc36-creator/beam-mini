package montafra.beam.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import montafra.beam.LocalHapticsEnabled
import montafra.beam.R
import montafra.beam.StatusService
import montafra.beam.settingsName
import montafra.beam.settingsUpdateInd
import montafra.beam.ui.theme.BeamCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(navController: BeamNavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences(settingsName, Context.MODE_PRIVATE) }

    val hapticsEnabled = LocalHapticsEnabled.current
    val notificationToggleHaptic = { enabled: Boolean ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (hapticsEnabled) {
                view.performHapticFeedback(
                    if (enabled) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
                )
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    var notificationEnabled by remember { mutableStateOf(prefs.getBoolean("notificationEnabled", true)) }
    val version = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
        catch (_: Exception) { "" }
    }
    val currentVersionCode = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt() }
        catch (_: Exception) { 0 }
    }

    val applyNotificationEnabled = { enabled: Boolean ->
        notificationEnabled = enabled
        prefs.edit().putBoolean("notificationEnabled", enabled).commit()
        if (enabled) {
            context.startForegroundService(Intent(context, StatusService::class.java))
        } else {
            context.sendBroadcast(Intent(settingsUpdateInd).setPackage(context.packageName))
        }
        montafra.beam.BeamNotificationTileService.requestRefresh(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) applyNotificationEnabled(true)
    }

    val setNotificationEnabled = { enabled: Boolean ->
        if (enabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            applyNotificationEnabled(enabled)
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.settings),
        onBack = { navController.popBackStack() },
    ) {
            // Interface
            item {
                SectionHeader(stringResource(R.string.interfaceSection))
                Spacer(Modifier.height(8.dp))
                BeamCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.notification)) },
                        supportingContent = { Text(stringResource(R.string.notificationEnableDesc)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ico_notification),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { enabled ->
                                    notificationToggleHaptic(enabled)
                                    setNotificationEnabled(enabled)
                                },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            val next = !notificationEnabled
                            notificationToggleHaptic(next)
                            setNotificationEnabled(next)
                        },
                    )
                }
            }

            // Workarounds
            item {
                SectionHeader(stringResource(R.string.advancedSection))
                Spacer(Modifier.height(8.dp))
                BeamCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.workarounds)) },
                        supportingContent = { Text(stringResource(R.string.workaroundsShortDesc)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ico_settings),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("settings/workarounds")
                        },
                    )
                }
            }

    }

}

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
internal fun SubLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

