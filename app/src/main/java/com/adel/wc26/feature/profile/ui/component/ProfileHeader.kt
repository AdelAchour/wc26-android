package com.adel.wc26.feature.profile.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.util.WC26DateTime

/**
 * The identity block at the top of a profile — avatar, display name,
 * @username, joined date. Shared by the own-profile and public-profile
 * screens so both look identical.
 */
@Composable
fun ProfileHeader(
    displayName: String,
    username: String,
    avatarUrl: String?,
    joinedAtIso: String,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.md))

        // Wrap avatar in an interactive box if onAvatarClick is not null
        Box(
            modifier = if (onAvatarClick != null) {
                Modifier.clickable { onAvatarClick() }
            } else {
                Modifier
            },
            contentAlignment = Alignment.BottomEnd
        ) {
            WC26Avatar(
                displayName = displayName,
                avatarUrl = avatarUrl,
                size = 90.dp,
            )
            // Pencil edit overlay
            if (onAvatarClick != null) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Avatar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "@$username",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = stringResource(
                R.string.profile_joined_on,
                WC26DateTime.calendarDate(joinedAtIso),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileHeaderPreview() {
    WC26Theme {
        ProfileHeader(
            displayName = "Adel",
            username = "adel",
            avatarUrl = null,
            joinedAtIso = "2026-05-01T10:00:00Z",
            onAvatarClick = {}
        )
    }
}