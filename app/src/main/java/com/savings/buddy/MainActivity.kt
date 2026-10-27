package com.savings.buddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.savings.buddy.ui.AppRoot
import com.savings.buddy.ui.theme.SavingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SavingApp).container
        setContent {
            val themeMode by container.settingsStore.theme
                .flowOn(Dispatchers.IO)
                .collectAsState(initial = 0)
            SavingTheme(themeMode = themeMode) {
                AppRoot(container = container)
            }
        }
    }
}
