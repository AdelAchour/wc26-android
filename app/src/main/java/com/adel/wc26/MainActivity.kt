package com.adel.wc26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.navigation.WC26NavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single Activity. Hosts the entire Compose UI and the navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WC26Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WC26NavHost()
                }
            }
        }
    }
}