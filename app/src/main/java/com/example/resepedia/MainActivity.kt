package com.example.resepedia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.navigation.AppNavigation
import com.example.resepedia.ui.theme.ResepediaTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasang splash screen sistem sebelum super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi preferensi bahasa yang tersimpan
        val savedLang = viewModel.getLanguage()
        if (savedLang.isNotBlank()) {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (appLocales.isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            // Menentukan tema berdasarkan preferensi (Ikuti Sistem, Terang, Gelap)
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            ResepediaTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    viewModel = viewModel
                )
            }
        }
    }

    /**
     * Mengubah bahasa aplikasi secara instan dan persisten
     */
    fun updateAppLanguage(languageCode: String) {
        viewModel.setLanguage(languageCode)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }
}
