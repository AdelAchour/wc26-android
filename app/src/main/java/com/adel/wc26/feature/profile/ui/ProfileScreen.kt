package com.adel.wc26.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.component.WC26EmptyState
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.profile.domain.UserProfile

/**
 * Profile tab — stateful entry point.
 */
@Composable
fun ProfileScreen(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onSignIn = onSignIn,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}

/**
 * Profile tab — stateless content. Renders one of: logged-out prompt,
 * loading, error, or the identity card.
 */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loggedOut -> LoggedOutProfile(onSignIn = onSignIn, modifier = modifier)
        state.loading -> WC26LoadingState(modifier = modifier)
        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )
        state.profile != null -> ProfileIdentity(
            profile = state.profile,
            modifier = modifier,
        )
        else -> WC26EmptyState(
            title = stringResource(R.string.profile_empty_title),
            description = stringResource(R.string.profile_empty_desc),
            modifier = modifier,
        )
    }
}

/** The identity card — avatar, names, joined date. */
@Composable
private fun ProfileIdentity(
    profile: UserProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xl))

        WC26Avatar(
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl,
            size = 96.dp,
        )

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "@${profile.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.xl))
        HorizontalDivider()
        Spacer(Modifier.height(Spacing.lg))

        InfoRow(
            label = stringResource(R.string.profile_email),
            value = profile.email,
        )
        InfoRow(
            label = stringResource(R.string.profile_joined),
            value = WC26DateTime.calendarDate(profile.joinedAt),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** Shown when a logged-out user opens the Profile tab. */
@Composable
private fun LoggedOutProfile(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.profile_logged_out_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.profile_logged_out_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.lg),
        )
        WC26PrimaryButton(
            text = stringResource(R.string.profile_sign_in),
            onClick = onSignIn,
        )
    }
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun ProfileIdentityPreview() {
    WC26Theme {
        ProfileContent(
            state = ProfileUiState(
                loading = false,
                profile = UserProfile(
                    id = 1,
                    email = "adel@example.com",
                    username = "adel",
                    displayName = "Adel",
                    avatarUrl = null,
                    role = "user",
                    joinedAt = "2026-05-01T10:00:00Z",
                ),
            ),
            onSignIn = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileLoggedOutPreview() {
    WC26Theme {
        ProfileContent(
            state = ProfileUiState(loggedOut = true, loading = false),
            onSignIn = {},
            onRetry = {},
        )
    }
}