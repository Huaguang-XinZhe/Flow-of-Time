package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    switchState: Boolean? = null,
    onSwitchToggle: ((Boolean) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, color = Color.Gray)
            }
        }

        if (switchState != null && onSwitchToggle != null) {
            Switch(checked = switchState, onCheckedChange = onSwitchToggle)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreen() {
    Column {
        SettingItem(
            title = "Notification",
            subtitle = "Enable or disable push notifications",
            switchState = true,
            onSwitchToggle = { newState ->
                // Handle switch state change here
            }
        )

        SettingItem(
            title = "Sound",
            subtitle = "Enable or disable sound",
            switchState = false,
            onSwitchToggle = { newState ->
                // Handle switch state change here
            }
        )
    }
}
