package com.adel.wc26.feature.auth.ui.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.component.WC26TextField
import com.adel.wc26.core.designsystem.theme.Spacing

/**
 * Register screen. On success the user is signed in immediately (the
 * backend returns a token on register) — [onRegistered] routes into the app.
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) onRegistered()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.create_your_account),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.join_the_wc26_conversation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Spacer(Modifier.height(Spacing.xl))

        WC26TextField(
            value = state.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = stringResource(R.string.display_name),
            errorText = state.displayNameError,
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.username,
            onValueChange = viewModel::onUsernameChange,
            label = stringResource(R.string.username),
            errorText = state.usernameError,
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.email),
            keyboardType = KeyboardType.Email,
            errorText = state.emailError,
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.password),
            isPassword = true,
            errorText = state.passwordError,
        )

        state.formError?.let { error ->
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        WC26PrimaryButton(
            text = stringResource(R.string.create_account),
            onClick = viewModel::submit,
            enabled = state.canSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.md))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(onClick = onGoToLogin) {
                Text(stringResource(R.string.already_have_account_title))
            }
        }
    }
}