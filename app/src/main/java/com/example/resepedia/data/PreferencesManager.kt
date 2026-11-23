package com.example.resepedia.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT,
    DARK
}

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("resepedia_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _favoriteIds = MutableStateFlow(loadFavoriteIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _isShakeEnabled = MutableStateFlow(loadShakeEnabled())
    val isShakeEnabled: StateFlow<Boolean> = _isShakeEnabled.asStateFlow()

    private val _searchHistory = MutableStateFlow(loadSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val gson = Gson()

    /**
     * Memuat tema tersimpan (LIGHT atau DARK).
     * Aman dari migrasi: jika user sebelumnya menyimpan "SYSTEM" atau data tidak dikenal,
     * otomatis mendeteksi kondisi tema sistem saat itu, lalu menyimpan hasilnya secara permanen.
     * Default untuk instalasi baru juga mengikuti kondisi sistem saat pertama kali dibuka.
     */
    private fun loadThemeMode(): AppThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, null)
        if (modeStr != null && modeStr != "SYSTEM") {
            try {
                return AppThemeMode.valueOf(modeStr)
            } catch (e: Exception) {
                // Abaikan bila string tersimpan tidak cocok dengan enum
            }
        }

        // Migrasi dari SYSTEM atau instalasi pertama kali: sesuaikan dengan tema sistem saat ini
        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val defaultMode = if (isSystemDark) AppThemeMode.DARK else AppThemeMode.LIGHT
        prefs.edit().putString(KEY_THEME_MODE, defaultMode.name).apply()
        return defaultMode
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

    fun isOnboardingDone(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
    }

    private fun loadShakeEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHAKE_ENABLED, true)
    }

    fun setShakeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply()
        _isShakeEnabled.value = enabled
    }

    private fun loadSearchHistory(): List<String> {
        val json = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Menambahkan kata kunci ke riwayat pencarian.
     * Maksimal 10 item terakhir, terbaru di atas, tanpa duplikat (case-insensitive).
     */
    fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = _searchHistory.value.toMutableList()
        // Hapus duplikat case-insensitive
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        // Sisipkan di posisi pertama (terbaru)
        current.add(0, trimmed)
        // Batasi maksimal 10 item
        val trimmedList = current.take(10)

        prefs.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(trimmedList)).apply()
        _searchHistory.value = trimmedList
    }

    /**
     * Menghapus satu item dari riwayat pencarian.
     */
    fun removeSearchHistory(query: String) {
        val current = _searchHistory.value.toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        prefs.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(current)).apply()
        _searchHistory.value = current
    }

    /**
     * Menghapus seluruh riwayat pencarian.
     */
    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
        _searchHistory.value = emptyList()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_SHAKE_ENABLED = "key_shake_enabled"
        private const val KEY_SEARCH_HISTORY = "key_search_history"
    }
}
