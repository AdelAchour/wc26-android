package com.adel.wc26.feature.auth.ui.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.component.WC26SecondaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme

/**
 * Welcome screen — the first thing a signed-out user sees.
 *
 * Three paths out: create an account, log in, or explore without an
 * account (browsing is allowed; gated actions prompt sign-in later).
 */
@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogIn: () -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xxxl))

        // Wordmark — echoes the web brand.
        Text(
            text = buildBrand(),
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))

        WC26PrimaryButton(
            text = stringResource(R.string.create_account),
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.md))

        WC26SecondaryButton(
            text = stringResource(R.string.log_in),
            onClick = onLogIn,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.sm))

        TextButton(onClick = onExplore) {
            Text(stringResource(R.string.explore_without_account))
        }

        Spacer(Modifier.height(Spacing.lg))
    }
}

/** "WC26" with the "26" in the accent color. */
@Composable
private fun buildBrand() = androidx.compose.ui.text.buildAnnotatedString {
    append("WC")
    withStyle(
        androidx.compose.ui.text.SpanStyle(
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
    ) {
        append("26")
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WC26Theme {
        WelcomeScreen(
            onCreateAccount = {},
            onLogIn = {},
            onExplore = {},
        )
    }
}