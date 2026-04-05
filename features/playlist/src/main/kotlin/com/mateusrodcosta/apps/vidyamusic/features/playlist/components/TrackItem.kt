package com.mateusrodcosta.apps.vidyamusic.features.playlist.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import com.mateusrodcosta.apps.vidyamusic.features.playlist.R as RPlaylist

@Composable
fun TrackItem(
    track: TrackEntity,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            if (track.comp.isNotEmpty()) {
                val creditText =
                    if (track.arr != null)
                        stringResource(
                            RPlaylist.string.playlist_track_item_credit_text,
                            track.comp, track.arr!!
                        )
                    else track.comp
                Text(
                    text = creditText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        overlineContent =
            {
                Text(
                    text = track.game.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
        colors = ListItemDefaults.colors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            headlineColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified,
            supportingColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified,
            overlineColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
        ),
    )
}
