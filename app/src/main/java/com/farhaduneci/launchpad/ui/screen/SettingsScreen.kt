package com.farhaduneci.launchpad.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farhaduneci.launchpad.data.ACTION_LOCK_SCREEN
import com.farhaduneci.launchpad.data.SLOT_BATTERY
import com.farhaduneci.launchpad.data.SLOT_BOT_LEFT
import com.farhaduneci.launchpad.data.SLOT_BOT_RIGHT
import com.farhaduneci.launchpad.data.SLOT_CLOCK
import com.farhaduneci.launchpad.data.SLOT_DATE
import com.farhaduneci.launchpad.data.SLOT_DOUBLE_TAP
import com.farhaduneci.launchpad.data.SLOT_MID_LEFT
import com.farhaduneci.launchpad.data.SLOT_MID_RIGHT
import com.farhaduneci.launchpad.data.SLOT_TOP_LEFT
import com.farhaduneci.launchpad.data.SLOT_TOP_RIGHT
import com.farhaduneci.launchpad.ui.LauncherViewModel
import com.farhaduneci.launchpad.ui.component.ColorPickerDialog
import com.farhaduneci.launchpad.util.colorToHex
import com.farhaduneci.launchpad.util.toTitleCase
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    currentFont: FontFamily,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    widgetColor: Color,
    onOpenSlotPicker: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val globalScale by viewModel.globalScale.collectAsState()
    val showClock by viewModel.showClock.collectAsState()
    val showDate by viewModel.showDate.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val slotsLocked by viewModel.slotsLocked.collectAsState()
    val fontIndex by viewModel.fontIndex.collectAsState()
    val bgTransparent by viewModel.bgTransparent.collectAsState()
    val slotStates by viewModel.slotStates.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val renameVersion by viewModel.renameVersion.collectAsState()

    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
        catch (_: Exception) { "" }
    }
    val fontNames = remember { listOf("Default", "Serif", "Mono", "Scope") }
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var showSlotMenu by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    val cardBg = textColor.copy(alpha = 0.05f)
    val dividerColor = textColor.copy(alpha = 0.15f)
    val secondaryText = textColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "←",
                    color = textColor,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { onDismiss() }.padding(8.dp)
                )
                Text(
                    text = "Settings",
                    color = textColor,
                    fontSize = 18.sp,
                    fontFamily = currentFont,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Appearance
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fontNames.forEachIndexed { i, name ->
                            FilterChip(
                                selected = fontIndex == i,
                                onClick = { viewModel.setFontIndex(i) },
                                label = { Text(name, fontFamily = currentFont) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = accentColor
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Size",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = globalScale,
                            onValueChange = { viewModel.setGlobalScale(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                        Text(
                            text = "${(globalScale * 100).roundToInt()}%",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = dividerColor)
                    SettingsToggleRow("Transparent Background", bgTransparent, accentColor, textColor) {
                        viewModel.setBgTransparent(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorDot(
                            label = "BG",
                            color = bgColor,
                            textColor = if (bgTransparent) textColor.copy(alpha = 0.3f) else textColor,
                            onClick = { if (!bgTransparent) showColorPicker = "bg" }
                        )
                        ColorDot(label = "Text", color = textColor, textColor = textColor) { showColorPicker = "text" }
                        ColorDot(label = "Accent", color = accentColor, textColor = textColor) { showColorPicker = "accent" }
                        ColorDot(label = "Clock", color = widgetColor, textColor = textColor) { showColorPicker = "widget" }
                    }
                }
            }

            // Display
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Display",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        Triple("Show Clock", showClock, viewModel::setShowClock),
                        Triple("Show Date", showDate, viewModel::setShowDate),
                        Triple("Show Battery", showBattery, viewModel::setShowBattery)
                    ).forEach { (label, value, setter) ->
                        SettingsToggleRow(label, value, accentColor, textColor, setter)
                    }
                }
            }

            // Slots
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Slots",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsToggleRow("Lock Slots", slotsLocked, accentColor, textColor) {
                        viewModel.setSlotsLocked(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showSlotMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f))
                    ) {
                        Text("Assign Slots", color = accentColor, fontFamily = currentFont)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Launchpad v$versionName",
                color = secondaryText,
                fontSize = 12.sp,
                fontFamily = currentFont,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Privacy Policy",
                color = accentColor,
                fontSize = 12.sp,
                fontFamily = currentFont,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showPrivacyPolicy = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Slot assignment dialog
    if (showSlotMenu) {
        val slotLabels = listOf(
            SLOT_TOP_LEFT to "Top Left", SLOT_MID_LEFT to "Mid Left", SLOT_BOT_LEFT to "Bot Left",
            SLOT_TOP_RIGHT to "Top Right", SLOT_MID_RIGHT to "Mid Right", SLOT_BOT_RIGHT to "Bot Right",
            SLOT_DOUBLE_TAP to "Double Tap",
            SLOT_CLOCK to "Clock", SLOT_DATE to "Date", SLOT_BATTERY to "Battery"
        )
        val appsWithRenames = remember(allApps, renameVersion) {
            allApps.associateBy({ it.first }, { (pkg, name) -> viewModel.getRename(pkg, name) })
        }
        AlertDialog(
            onDismissRequest = { showSlotMenu = false },
            containerColor = bgColor,
            title = { Text("Assign Slots", color = textColor, fontFamily = currentFont) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    slotLabels.forEach { (id, label) ->
                        val selectedPkg = slotStates[id]
                        val selectedName = selectedPkg?.let { pkg ->
                            when (pkg) {
                                ACTION_LOCK_SCREEN -> "Lock Screen"
                                else -> appsWithRenames[pkg]?.let { toTitleCase(it) }
                            }
                        } ?: "None"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSlotMenu = false
                                    onOpenSlotPicker(id)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = label, color = accentColor, fontSize = 14.sp)
                            Text(text = selectedName, color = secondaryText, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSlotMenu = false }) {
                    Text("Done", color = accentColor)
                }
            }
        )
    }

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            containerColor = bgColor,
            title = { Text("Privacy Policy", color = textColor, fontFamily = currentFont) },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    item {
                        Text(
                            text = """We do not collect, store, or share any personal data. All app data is stored locally on your device. We have no servers and no backend.

This app does not use the internet.

We do not use any analytics, advertising, crash reporting, or third-party SDKs.

All data remains on your device. Uninstalling the app removes all locally stored data.

Our apps do not collect any personal information from anyone, including children under the age of 13.

Contact: literalapps@proton.me""",
                            color = textColor,
                            fontFamily = currentFont,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text("Close", color = accentColor)
                }
            }
        )
    }

    // Color picker
    showColorPicker?.let { target ->
        val initialColor = when (target) {
            "bg" -> bgColor
            "text" -> textColor
            "widget" -> widgetColor
            else -> accentColor
        }
        ColorPickerDialog(
            initialColor = initialColor,
            textColor = textColor,
            accentColor = accentColor,
            onColorSelected = { color ->
                val hex = colorToHex(color)
                when (target) {
                    "bg" -> viewModel.setBgColor(hex)
                    "text" -> viewModel.setTextColor(hex)
                    "widget" -> viewModel.setWidgetColor(hex)
                    else -> viewModel.setAccentColor(hex)
                }
                showColorPicker = null
            },
            onDismiss = { showColorPicker = null }
        )
    }
}

@Composable
private fun ColorDot(label: String, color: Color, textColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, textColor.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    value: Boolean,
    accentColor: Color,
    textColor: Color,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.5f)
            )
        )
    }
}
