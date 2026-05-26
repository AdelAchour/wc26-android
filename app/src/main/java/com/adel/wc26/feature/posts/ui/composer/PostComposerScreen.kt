package com.adel.wc26.feature.posts.ui.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes

/**
 * Post composer — stateful entry point. On a successful post it invokes
 * [onPosted] so the host can pop back to the match thread.
 */
@Composable
fun PostComposerScreen(
    onClose: () -> Unit,
    onPosted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostComposerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) {
        if (state.done) onPosted()
    }

    PostComposerContent(
        state = state,
        onTextChange = viewModel::onTextChange,
        onSubmit = viewModel::submit,
        onClose = onClose,
        modifier = modifier,
    )
}

/**
 * Post composer — stateless content. A close action, a text area, a live
 * character counter, and a post action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposerContent(
    state: ComposerUiState,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.composer_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSubmit,
                        enabled = state.canSubmit,
                    ) {
                        Text(stringResource(R.string.composer_post))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg),
        ) {
            BasicTextField(
                value = state.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                decorationBox = { inner ->
                    if (state.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.composer_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )

            Spacer(Modifier.height(Spacing.md))

            // Character counter — turns red when near/over the limit.
            Text(
                text = state.charsLeft.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (state.charsLeft < 0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )

            state.error?.let { error ->
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(error.toStringRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostComposerPreview() {
    WC26Theme {
        PostComposerContent(
            state = ComposerUiState(text = "Canada are looking dangerous tonight!"),
            onTextChange = {},
            onSubmit = {},
            onClose = {},
        )
    }
}