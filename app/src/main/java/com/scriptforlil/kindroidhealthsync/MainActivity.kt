package com.scriptforlil.kindroidhealthsync

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scriptforlil.kindroidhealthsync.data.AppContainer
import com.scriptforlil.kindroidhealthsync.ui.MainScreen
import com.scriptforlil.kindroidhealthsync.ui.MainViewModel
import com.scriptforlil.kindroidhealthsync.ui.MainViewModelFactory
import com.scriptforlil.kindroidhealthsync.ui.theme.KindroidHealthTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val container = AppContainer(applicationContext)
        val languageTag = runBlocking {
            container.settingsRepository.settings.first().appLanguage
        }

        AppCompatDelegate.setApplicationLocales(
            if (languageTag == "system") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(languageTag)
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            val savedDarkTheme by container.settingsRepository.darkThemeEnabled.collectAsState(initial = null)
            val savedHeroExpanded by container.settingsRepository.heroExpanded.collectAsState(initial = null)
            val settings by container.settingsRepository.settings.collectAsState(initial = null)
            val coroutineScope = rememberCoroutineScope()
            val darkThemeEnabled = savedDarkTheme ?: systemDarkTheme
            val heroExpanded = savedHeroExpanded ?: true

            val currentLanguageTag = settings?.appLanguage ?: languageTag
            AppCompatDelegate.setApplicationLocales(
                if (currentLanguageTag == "system") LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(currentLanguageTag)
            )

            KindroidHealthTheme(darkTheme = darkThemeEnabled) {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(container)
                )
                MainScreen(
                    viewModel = viewModel,
                    darkThemeEnabled = darkThemeEnabled,
                    heroExpanded = heroExpanded,
                    onHeroExpandedChange = { expanded ->
                        coroutineScope.launch {
                            container.settingsRepository.saveHeroExpanded(expanded)
                        }
                    },
                    onDarkThemeChange = { enabled ->
                        coroutineScope.launch {
                            container.settingsRepository.saveDarkThemeEnabled(enabled)
                        }
                    }
                )
            }
        }
    }
}
