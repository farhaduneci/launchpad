package com.farhaduneci.launchpad.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farhaduneci.launchpad.data.ACTION_LOCK_SCREEN
import com.farhaduneci.launchpad.util.toTitleCase

private val SLOT_ACTIONS = listOf(
    ACTION_LOCK_SCREEN to "Lock Screen"
)

@Composable
fun AppPickerDialog(
    slotName: String,
    currentFont: FontFamily,
    globalScale: Float,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    apps: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    val secondaryText = textColor.copy(alpha = 0.5f)
    val dividerColor = textColor.copy(alpha = 0.15f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        title = { Text("Set $slotName", color = textColor, fontFamily = currentFont) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                // None
                item(key = "__none__") {
                    Text(
                        text = "None",
                        color = secondaryText,
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null) }
                            .padding(vertical = 12.dp)
                    )
                }

                // Actions section
                item(key = "__actions_header__") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    Text(
                        text = "Actions",
                        color = accentColor.copy(alpha = 0.7f),
                        fontSize = (12 * globalScale).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(SLOT_ACTIONS, key = { it.first }) { (id, label) ->
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 12.dp)
                    )
                }

                // Apps section
                item(key = "__apps_header__") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    Text(
                        text = "Apps",
                        color = accentColor.copy(alpha = 0.7f),
                        fontSize = (12 * globalScale).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(apps, key = { it.first }) { (pkg, displayName) ->
                    Text(
                        text = toTitleCase(displayName),
                        color = textColor,
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(pkg) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accentColor)
            }
        }
    )
}
