package com.mateusrodcosta.apps.vidyamusic.features.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import com.mateusrodcosta.apps.vidyamusic.features.shared.utils.uiInfo
import com.mateusrodcosta.apps.vidyamusic.features.settings.R as RSettings
import com.mateusrodcosta.apps.vidyamusic.features.shared.R as RShared


@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(RSettings.string.settings_theme_selector_header))
        },
        text = {
            LazyColumn {
                val availableThemes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)

                items(availableThemes) { theme ->
                    ThemeOption(
                        theme,
                        isCurrent = currentTheme == theme,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(RShared.string.button_cancel))
            }
        }
    )
}

@Composable
private fun ThemeOption(
    theme: ThemeMode,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = theme.uiInfo

    ListItem(
        headlineContent = {
            Text(
                text = info.text,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(info.icon, contentDescription = info.label)
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            leadingIconColor = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
            headlineColor = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified,
            supportingColor = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
    )
}