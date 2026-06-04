package com.adel.wc26.feature.profile.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.AvatarPreset
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.component.isAvatarPreset
import com.adel.wc26.core.designsystem.component.removePresetPrefix
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes

/**
 * Stateful wrapper for AvatarPickerScreen. Sets up ViewModel state collection
 * and navigation callback triggers.
 */
@Composable
fun AvatarPickerScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val updateSuccessMsg = stringResource(R.string.avatar_picker_success_update)
    // Trigger toast and pop backstack on success
    var wasUpdating by remember { mutableStateOf(false) }
    LaunchedEffect(state.isUpdatingAvatar) {
        if (wasUpdating && !state.isUpdatingAvatar) {
            if (state.avatarError == null) {
                Toast.makeText(context, updateSuccessMsg, Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
        wasUpdating = state.isUpdatingAvatar
    }

    // Show error toast if saving fails
    LaunchedEffect(state.avatarError) {
        state.avatarError?.let {
            Toast.makeText(context, context.getString(it.toStringRes()), Toast.LENGTH_SHORT).show()
        }
    }

    AvatarPickerContent(
        displayName = state.profile?.displayName ?: "User",
        currentAvatarUrl = state.profile?.avatarUrl,
        isUpdatingAvatar = state.isUpdatingAvatar,
        onBack = onBack,
        onSaveAvatar = { newPresetUrl ->
            viewModel.updateAvatar(newPresetUrl)
        },
        modifier = modifier
    )
}

/**
 * Stateless UI content for AvatarPickerScreen. Previewable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerContent(
    displayName: String,
    currentAvatarUrl: String?,
    isUpdatingAvatar: Boolean,
    onBack: () -> Unit,
    onSaveAvatar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for selecting items inside the grid
    var selectedPresetKey by remember(currentAvatarUrl) {
        val defaultOrCurrent = if (currentAvatarUrl?.isAvatarPreset() == true) {
            currentAvatarUrl.removePresetPrefix()
        } else {
            "avatar_soccer_ball"
        }
        mutableStateOf(defaultOrCurrent)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.avatar_picker_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Spacing.md))

            // Section: Current Selection Preview
            Text(
                text = stringResource(R.string.avatar_picker_preview_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.md))

            Box(contentAlignment = Alignment.Center) {
                WC26Avatar(
                    displayName = displayName,
                    avatarUrl = "preset://$selectedPresetKey",
                    size = 112.dp
                )
                if (isUpdatingAvatar) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Section: Selection Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.weight(1f)
            ) {
                items(AvatarPreset.entries) { item ->
                    val isSelected = selectedPresetKey == item.key
                    val cardBorderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    }
                    val cardBgColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Spacing.md))
                            .background(cardBgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = cardBorderColor,
                                shape = RoundedCornerShape(Spacing.md)
                            )
                            .clickable(enabled = !isUpdatingAvatar) {
                                selectedPresetKey = item.key
                            }
                            .padding(Spacing.md)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            WC26Avatar(
                                displayName = displayName,
                                avatarUrl = "preset://${item.key}",
                                size = 56.dp
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(Spacing.sm))

                        Text(
                            text = stringResource(item.labelResId),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            // Section: Save Button
            val hasChanges = currentAvatarUrl != "preset://$selectedPresetKey"
            WC26PrimaryButton(
                text = if (isUpdatingAvatar) {
                    stringResource(R.string.avatar_picker_saving)
                } else {
                    stringResource(R.string.avatar_picker_save)
                },
                enabled = hasChanges && !isUpdatingAvatar,
                onClick = {
                    onSaveAvatar("preset://$selectedPresetKey")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarPickerPreview() {
    WC26Theme {
        AvatarPickerContent(
            displayName = "Adel",
            currentAvatarUrl = "preset://avatar_soccer_ball",
            isUpdatingAvatar = false,
            onBack = {},
            onSaveAvatar = {}
        )
    }
}