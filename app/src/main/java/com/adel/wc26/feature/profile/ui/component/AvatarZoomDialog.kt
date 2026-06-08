package com.adel.wc26.feature.profile.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.AvatarPreset
import com.adel.wc26.core.designsystem.component.isAvatarPreset
import com.adel.wc26.core.designsystem.component.removePresetPrefix
import com.adel.wc26.core.designsystem.theme.Spacing

@Composable
fun AvatarZoomDialog(
    displayName: String,
    avatarUrl: String?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Spacing.lg),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val preset = remember(avatarUrl) {
                    if (avatarUrl?.isAvatarPreset() == true) {
                        AvatarPreset.fromKey(avatarUrl.removePresetPrefix())
                    } else {
                        null
                    }
                }

                if (preset != null) {
                    Image(
                        painter = painterResource(id = preset.drawableResId),
                        contentDescription = stringResource(preset.labelResId),
                        modifier = Modifier
                            .fillMaxWidth()
                            //.aspectRatio(1f)
                            .clip(RoundedCornerShape(Spacing.md)),
                        //contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(preset.labelResId),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Fallback to stylized letter avatar if no preset is set
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(Spacing.md))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.action_close),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}