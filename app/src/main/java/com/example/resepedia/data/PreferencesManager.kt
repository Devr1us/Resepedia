package com.example.resepedia.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("resepedia_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _favoriteIds = MutableStateFlow(loadFavoriteIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(modeStr ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleFavorite(mealId: String): Boolean {
        val current = _favoriteIds.value.toMutableSet()
        val isNowFavorite: Boolean
        if (current.contains(mealId)) {
            current.remove(mealId)
            isNowFavorite = false
        } else {
            current.add(mealId)
            isNowFavorite = true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoriteIds.value = current
        return isNowFavorite
    }

    fun isFavorite(mealId: String): Boolean {
        return _favoriteIds.value.contains(mealId)
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_LANGUAGE = "key_language"
    }
}
