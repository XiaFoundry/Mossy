package com.savings.buddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.savings.buddy.ui.AppRoot
import com.savings.buddy.ui.i18n.LocalAppLocale
import com.savings.buddy.ui.i18n.LocalStrings
import com.savings.buddy.ui.i18n.localeFor
import com.savings.buddy.ui.i18n.resolveLanguageTag
import com.savings.buddy.ui.i18n.stringsFor
import com.savings.buddy.ui.theme.SavingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SavingApp).container
        setContent {
            val themeMode by container.settingsStore.theme
                .flowOn(Dispatchers.IO)
                .collectAsState(initial = 0)
            val palette by container.settingsStore.palette
                .flowOn(Dispatchers.IO)
                .collectAsState(initial = 0)
            val languagePref by container.settingsStore.language
                .flowOn(Dispatchers.IO)
                .collectAsState(initial = 0)
            val tag = resolveLanguageTag(languagePref, Locale.getDefault())
            val strings = stringsFor(tag)
            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalAppLocale provides localeFor(tag),
            ) {
                SavingTheme(themeMode = themeMode, palette = palette) {
                    AppRoot(container = container)
                }
            }
        }
    }
}
