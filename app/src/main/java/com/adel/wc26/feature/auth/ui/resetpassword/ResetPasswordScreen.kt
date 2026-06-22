package com.adel.wc26.feature.auth.ui.resetpassword

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
 * Reset Password screen — stateful entry point. Collects the ViewModel
 * state and delegates rendering to [ResetPasswordContent].
 */
@Composable
fun ResetPasswordScreen(
    onPasswordReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(
                context,
                context.getString(R.string.reset_password_success),
                Toast.LENGTH_LONG,
            ).show()
            onPasswordReset()
        }
    }

    ResetPasswordContent(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Reset Password screen — stateless content. A pure function of
 * [ResetPasswordUiState]; previewable without Hilt.
 */
@Composable
fun ResetPasswordContent(
    state: ResetPasswordUiState,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
    ) {
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.reset_password_title),
            style = MaterialTheme.typography.displaySmall,
        )

        Spacer(Modifier.height(Spacing.md))

        // Info card about the reset code
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.reset_password_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.md),
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        WC26TextField(
            value = state.email,
            onValueChange = {},
            label = stringResource(R.string.field_email),
            keyboardType = KeyboardType.Email,
            singleLine = true,
        )

        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.code,
            onValueChange = onCodeChange,
            label = stringResource(R.string.reset_password_code_label),
            keyboardType = KeyboardType.Number,
            errorText = state.codeError?.let { stringResource(it.toStringRes()) },
        )

        Spacer(Modifier.height(Spacing.sm))

        WC26TextField(
            value = state.newPassword,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.reset_password_new_password_label),
            isPassword = true,
            errorText = state.passwordError?.let { stringResource(it.toStringRes()) },
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
            text = stringResource(R.string.reset_password_action),
            onClick = onSubmit,
            enabled = state.canSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetPasswordContentPreview() {
    WC26Theme {
        ResetPasswordContent(
            state = ResetPasswordUiState(
                email = "fan@example.com",
                code = "123456",
                newPassword = "newpassword",
            ),
            onCodeChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
