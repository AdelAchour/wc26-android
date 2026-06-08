package com.adel.wc26.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "wc26_theme_prefs")

enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_config")
    }

    val themeFlow: Flow<DarkThemeConfig> = context.themeDataStore.data.map { prefs ->
        val saved = prefs[KEY_THEME]
        if (saved != null) {
            try {
                DarkThemeConfig.valueOf(saved)
            } catch (e: Exception) {
                DarkThemeConfig.FOLLOW_SYSTEM
            }
        } else {
            DarkThemeConfig.FOLLOW_SYSTEM
        }
    }

    suspend fun setTheme(config: DarkThemeConfig) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = config.name
        }
    }
}
