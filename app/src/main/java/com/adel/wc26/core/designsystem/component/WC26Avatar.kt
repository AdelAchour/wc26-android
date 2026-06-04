package com.adel.wc26.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.WC26Theme


/**
 * Single source of truth for the World Cup-themed avatar presets.
 * Add new presets here to automatically expose them to both rendering and selection screens.
 */
enum class AvatarPreset(
    val key: String,
    val drawableResId: Int,
    val labelResId: Int
) {
    SOCCER_BALL("avatar_soccer_ball", R.drawable.avatar_soccer_ball, R.string.avatar_label_soccer_ball),
    TROPHY("avatar_trophy", R.drawable.avatar_trophy, R.string.avatar_label_trophy),
    REFEREE("avatar_referee", R.drawable.avatar_referee, R.string.avatar_label_referee),
    JERSEY_RED("avatar_jersey_red", R.drawable.avatar_jersey_red, R.string.avatar_label_jersey_red),
    JERSEY_BLUE("avatar_jersey_blue", R.drawable.avatar_jersey_blue, R.string.avatar_label_jersey_blue),
    //JERSEY_YELLOW("avatar_jersey_yellow", R.drawable.avatar_jersey_yellow, R.string.avatar_label_jersey_yellow),
    STADIUM("avatar_stadium", R.drawable.avatar_stadium, R.string.avatar_label_stadium);
    companion object {
        fun fromKey(key: String): AvatarPreset? {
            return entries.find { it.key == key }
        }
    }
}

fun String.isAvatarPreset() : Boolean {
    return this.startsWith("preset://")
}

fun String.removePresetPrefix() : String {
    return this.removePrefix("preset://")
}

/**
 * A circular avatar. Shows the user's image when [avatarUrl] is present:
 * - If it starts with "preset://", resolves the drawable dynamically.
 * - Otherwise loaded with Coil as a remote/real URL.
 * Falls back to the first letter of [displayName] on a colored circle if no avatar.
 */
@Composable
fun WC26Avatar(
    displayName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    // Setup a common modifier for styling the circle and drawing the border
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            shape = CircleShape
        )

    val preset = if (avatarUrl?.isAvatarPreset() == true) {
        val presetName = avatarUrl.removePresetPrefix()
        AvatarPreset.fromKey(presetName)
    } else {
        null
    }
    if (preset != null) {
        Image(
            painter = painterResource(id = preset.drawableResId),
            contentDescription = null,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        DefaultLetterAvatar(displayName, avatarModifier)
    }
}

@Composable
private fun DefaultLetterAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = displayName.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WC26AvatarPreview() {
    WC26Theme {
        WC26Avatar(
            displayName = "Adel",
            avatarUrl = "preset://avatar_soccer_ball",
        )
    }
}