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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    THE_CAPTAIN(
        "the_captain",
        R.drawable.the_captain,
        R.string.avatar_label_the_captain
    ),

    THE_WONDERKID(
        "the_wonderkid",
        R.drawable.the_wonderkid,
        R.string.avatar_label_the_wonderkid
    ),

    THE_WALL(
        "the_wall",
        R.drawable.the_wall,
        R.string.avatar_label_the_wall
    ),

    THE_WHISTLE(
        "the_whistle",
        R.drawable.the_whistle,
        R.string.avatar_label_the_whistle
    ),

    THE_PITCH(
        "the_pitch",
        R.drawable.the_pitch,
        R.string.avatar_label_the_pitch
    ),

    THE_GOLDEN_BOOT(
        "the_golden_boot",
        R.drawable.the_golden_boot,
        R.string.avatar_label_the_golden_boot
    ),

    THE_UNDERDOG(
        "the_underdog",
        R.drawable.the_underdog,
        R.string.avatar_label_the_underdog
    ),

    THE_DARK_HORSE(
        "the_dark_horse",
        R.drawable.the_dark_horse,
        R.string.avatar_label_the_dark_horse
    ),

    VAR_ROBOT(
        "the_var",
        R.drawable.the_var,
                R.string.avatar_label_the_var
    ),

    THE_SCOUT(
        "the_scout",
        R.drawable.the_scout,
        R.string.avatar_label_the_scout
    ),

    THE_ROAR(
        "the_roar",
        R.drawable.the_roar,
        R.string.avatar_label_the_roar
    ),

    THE_TROPHY_HUNTER(
        "the_trophy_hunter",
        R.drawable.the_trophy_hunter,
        R.string.avatar_label_the_trophy_hunter
    ),

    THE_SUPER_SUB(
        "the_super_sub",
        R.drawable.the_super_sub,
        R.string.avatar_label_the_super_sub
    ),

    THE_TACTICIAN(
        "the_tactician",
        R.drawable.the_tactician,
        R.string.avatar_label_the_tactician
    ),

    THE_PENALTY_KING(
        "the_penalty_king",
        R.drawable.the_penalty_king,
        R.string.avatar_label_the_penalty_king
    ),

    THE_COMMENTATOR(
        "the_commentator",
        R.drawable.the_commentator,
        R.string.avatar_label_the_commentator
    ),

    THE_TRANSFER_RUMOR(
        "the_transfer_rumor",
        R.drawable.the_transfer_rumor,
        R.string.avatar_label_the_Transfer_rumor
    );

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
            avatarUrl = "preset://the_captain",
        )
    }
}