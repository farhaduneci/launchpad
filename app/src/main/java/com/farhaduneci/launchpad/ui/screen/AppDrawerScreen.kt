package com.farhaduneci.launchpad.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.farhaduneci.launchpad.ui.LauncherViewModel
import com.farhaduneci.launchpad.util.launchMuDirect
import com.farhaduneci.launchpad.util.safeLower
import com.farhaduneci.launchpad.util.toTitleCase

@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    screenW: Int,
    globalScale: Float,
    currentFont: FontFamily,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    onAppClick: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSlotPicker: (String) -> Unit
) {
    val context = LocalContext.current
    val allApps by viewModel.allApps.collectAsState()
    val chestApps by viewModel.chestApps.collectAsState()
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val renameVersion by viewModel.renameVersion.collectAsState()
    val listState = rememberLazyListState()

    var selectedAppForMenu by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showChestItems by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Pinned: resolved names, preserving pin order, excluding chest
    val visiblePinned = remember(pinnedApps, chestApps, renameVersion, allApps) {
        pinnedApps
            .filter { it !in chestApps }
            .mapNotNull { pkg ->
                allApps.find { it.first == pkg }
                    ?.let { (_, name) -> pkg to viewModel.getRename(pkg, name) }
            }
    }

    // Regular: not pinned, not in chest, sorted alphabetically
    val visibleRegular = remember(pinnedApps, chestApps, renameVersion, allApps) {
        allApps
            .filter { it.first !in chestApps && it.first !in pinnedApps }
            .map { (pkg, name) -> pkg to viewModel.getRename(pkg, name) }
            .sortedBy { safeLower(it.second) }
    }

    // Pair each regular item with its section letter (non-null only for the first item in each letter group)
    val regularWithLetters: List<Pair<Pair<String, String>, Char?>> = remember(visibleRegular) {
        var lastLetter: Char? = null
        visibleRegular.map { item ->
            val letter = item.second.firstOrNull()?.uppercaseChar()
            val show = if (letter != lastLetter) { lastLetter = letter; letter } else null
            item to show
        }
    }

    val secondaryText = textColor.copy(alpha = 0.5f)
    val dividerColor = textColor.copy(alpha = 0.15f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onReturnToHome() }
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = (screenW * 0.06f).dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "Chest",
                tint = secondaryText,
                modifier = Modifier
                    .size((screenW * 0.06f * globalScale).dp)
                    .clickable { showChestItems = true }
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = textColor,
                modifier = Modifier
                    .size((screenW * 0.06f * globalScale).dp)
                    .clickable { onOpenSettings() }
            )
        }

        // ── App list with bottom fade ─────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (screenW * 0.06f).dp,
                        end = (screenW * 0.12f).dp,
                        bottom = 80.dp
                    )
            ) {
                // Pinned section
                if (visiblePinned.isNotEmpty()) {
                    items(visiblePinned, key = { "pin_${it.first}" }) { (pkg, displayName) ->
                        AppRow(
                            letter = null,
                            displayName = displayName,
                            textColor = textColor,
                            accentColor = accentColor,
                            screenW = screenW,
                            globalScale = globalScale,
                            currentFont = currentFont,
                            onTap = { launchMuDirect(context, pkg); onAppClick() },
                            onLongPress = { selectedAppForMenu = pkg to displayName }
                        )
                    }
                    item("pin_divider") {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = dividerColor
                        )
                    }
                }

                // Alphabetical section
                items(regularWithLetters, key = { it.first.first }) { (app, letter) ->
                    val (pkg, displayName) = app
                    AppRow(
                        letter = letter?.toString(),
                        displayName = displayName,
                        textColor = textColor,
                        accentColor = accentColor,
                        screenW = screenW,
                        globalScale = globalScale,
                        currentFont = currentFont,
                        onTap = { launchMuDirect(context, pkg); onAppClick() },
                        onLongPress = { selectedAppForMenu = pkg to displayName }
                    )
                }
            }

            // Bottom fade — list fades out instead of hard-cutting
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, bgColor))
                    )
            )
        }
    }

    // ── Context menu ─────────────────────────────────────────────────────────
    selectedAppForMenu?.let { (pkg, name) ->
        val isPinned = pkg in pinnedApps
        AlertDialog(
            onDismissRequest = { selectedAppForMenu = null },
            containerColor = bgColor,
            title = { Text(toTitleCase(name), color = textColor, fontFamily = currentFont) },
            text = {
                Column {
                    listOf(
                        "Rename" to {
                            showRenameDialog = pkg to name
                            selectedAppForMenu = null
                        },
                        (if (isPinned) "Unpin" else "Pin To Top") to {
                            if (isPinned) viewModel.unpin(pkg) else viewModel.pin(pkg)
                            selectedAppForMenu = null
                        },
                        "Send To Chest" to {
                            viewModel.addToChest(pkg)
                            selectedAppForMenu = null
                        },
                        "App Info" to {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData("package:$pkg".toUri())
                            )
                            selectedAppForMenu = null
                        }
                    ).forEach { (label, action) ->
                        Text(
                            text = label,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { action() }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    // ── Chest dialog ─────────────────────────────────────────────────────────
    if (showChestItems) {
        val appsInChest = remember(chestApps, renameVersion, allApps) {
            allApps
                .filter { it.first in chestApps }
                .map { (pkg, name) -> pkg to viewModel.getRename(pkg, name) }
                .sortedBy { safeLower(it.second) }
        }
        AlertDialog(
            onDismissRequest = { showChestItems = false },
            containerColor = bgColor,
            title = { Text("Chest", color = accentColor, fontFamily = currentFont) },
            text = {
                if (appsInChest.isEmpty()) {
                    Text("Chest Is Empty", color = secondaryText)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(appsInChest, key = { it.first }) { (pkg, displayName) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = toTitleCase(displayName),
                                    color = textColor,
                                    fontFamily = currentFont,
                                    fontSize = (screenW * 0.045f * globalScale).sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            launchMuDirect(context, pkg)
                                            onAppClick()
                                            showChestItems = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                                Text(
                                    text = "Restore",
                                    color = secondaryText,
                                    fontFamily = currentFont,
                                    fontSize = (screenW * 0.035f * globalScale).sp,
                                    modifier = Modifier
                                        .clickable { viewModel.removeFromChest(pkg) }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChestItems = false }) {
                    Text("Done", color = accentColor)
                }
            }
        )
    }

    // ── Rename dialog ─────────────────────────────────────────────────────────
    showRenameDialog?.let { (pkg, name) ->
        var text by remember(pkg) { mutableStateOf(viewModel.getRename(pkg, name)) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            containerColor = bgColor,
            title = { Text("Rename ${toTitleCase(name)}", color = textColor) },
            text = {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setRename(pkg, text.ifBlank { null })
                    showRenameDialog = null
                }) {
                    Text("Ok", color = accentColor)
                }
            }
        )
    }
}

@Composable
private fun AppRow(
    letter: String?,
    displayName: String,
    textColor: Color,
    accentColor: Color,
    screenW: Int,
    globalScale: Float,
    currentFont: FontFamily,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Section letter (always left of app name; blank when not the first in group)
        Text(
            text = letter ?: "",
            color = accentColor.copy(alpha = 0.7f),
            fontSize = (screenW * 0.030f * globalScale).sp,
            fontFamily = currentFont,
            modifier = Modifier.width((screenW * 0.08f).dp)
        )
        Text(
            text = toTitleCase(displayName),
            color = textColor,
            fontSize = (screenW * 0.045f * globalScale).sp,
            fontFamily = currentFont,
            modifier = Modifier.pointerInput(displayName) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
        )
    }
}
