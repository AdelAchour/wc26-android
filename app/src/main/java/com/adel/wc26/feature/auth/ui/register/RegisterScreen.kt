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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.component.WC26TextField
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes

/**
 * Register screen — stateful entry point.
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

    RegisterContent(
        state = state,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onGoToLogin = onGoToLogin,
        modifier = modifier,
    )
}

/**
 * Register screen — stateless content. Previewable without Hilt.
 */
@Composable
fun RegisterContent(
    state: RegisterUiState,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Spacer(Modifier.height(Spacing.xl))

        WC26TextField(
            value = state.displayName,
            onValueChange = onDisplayNameChange,
            label = stringResource(R.string.field_display_name),
            errorText = state.displayNameError?.let { stringResource(it.toStringRes()) },
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = stringResource(R.string.field_username),
            errorText = state.usernameError?.let { stringResource(it.toStringRes()) },
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            keyboardType = KeyboardType.Email,
            errorText = state.emailError?.let { stringResource(it.toStringRes()) },
        )
        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_password),
            isPassword = true,
            errorText = state.passwordError?.let { stringResource(it.toStringRes()) },
        )

        state.formError?.let { error ->
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(error.toStringRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        WC26PrimaryButton(
            text = stringResource(R.string.register_action),
            onClick = onSubmit,
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
                Text(stringResource(R.string.register_go_to_login))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentPreview() {
    WC26Theme {
        RegisterContent(
            state = RegisterUiState(
                displayName = "Adel",
                username = "adel",
                email = "adel@example.com",
                password = "secret123",
            ),
            onDisplayNameChange = {},
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onGoToLogin = {},
        )
    }
}