package com.adel.wc26.feature.status.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import androidx.core.net.toUri
import com.adel.wc26.BuildConfig
import com.adel.wc26.core.designsystem.theme.WC26Theme

@Composable
fun ForceUpdateScreen(
    updateUrl: String,
    minVersion: Int,
    modifier: Modifier = Modifier,
) {
    // Intercept and disable back button presses so the user cannot bypass the screen
    BackHandler { /* Do nothing */ }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.update_ball),
            contentDescription = null,
            modifier = modifier.width(200.dp),
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = stringResource(R.string.force_update_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = stringResource(R.string.force_update_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

//        Spacer(modifier = Modifier.height(Spacing.md))

//        Text(
//            text = stringResource(
//                R.string.force_update_version_info,
//                minVersion,
//                BuildConfig.VERSION_CODE
//            ),
//            style = MaterialTheme.typography.bodySmall,
//            fontWeight = FontWeight.Medium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
//            textAlign = TextAlign.Center
//        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        WC26PrimaryButton(
            text = stringResource(R.string.force_update_action),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, updateUrl.toUri())
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForceUpdateScreenPreview() {
    WC26Theme {
        ForceUpdateScreen(
            updateUrl = "https://google.com",
            minVersion = 1
        )
    }
}