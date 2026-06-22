package com.adel.wc26.feature.auth.ui.forgotpassword

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
import androidx.compose.ui.platform.LocalContext
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
import com.adel.wc26.core.ui.toDisplayString
import com.adel.wc26.core.ui.toStringRes

/**
 * Forgot Password screen — stateful entry point. Collects the ViewModel
 * state and delegates rendering to [ForgotPasswordContent].
 */
@Composable
fun ForgotPasswordScreen(
    onCodeSent: (email: String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) onCodeSent(state.email)
    }

    ForgotPasswordContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::submit,
        onBackToLogin = onBackToLogin,
        modifier = modifier,
    )
}

/**
 * Forgot Password screen — stateless content. A pure function of
 * [ForgotPasswordUiState]; previewable without Hilt.
 */
@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()
            )
            .padding(Spacing.xl),
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.forgot_password_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.forgot_password_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Spacer(Modifier.height(Spacing.xl))

        WC26TextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            keyboardType = KeyboardType.Email,
            errorText = state.emailError?.let { stringResource(it.toStringRes()) },
        )

        state.formError?.let { error ->
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = error.toDisplayString(context),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        WC26PrimaryButton(
            text = stringResource(R.string.forgot_password_action),
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
            TextButton(onClick = onBackToLogin) {
                Text(stringResource(R.string.login_action))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordContentPreview() {
    WC26Theme {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(email = "fan@example.com"),
            onEmailChange = {},
            onSubmit = {},
            onBackToLogin = {},
        )
    }
}
