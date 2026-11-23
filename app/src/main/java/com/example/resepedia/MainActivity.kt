package com.example.resepedia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.navigation.AppNavigation
import com.example.resepedia.ui.theme.ResepediaTheme
import com.example.resepedia.ui.util.LocalWindowSizeClass

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasang splash screen sistem sebelum super.onCreate()
        val splashScreen = installSplashScreen()

        var isAppReady = false
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            !isAppReady && (System.currentTimeMillis() - startTime < 1000L)
        }

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

        // Tandai bahwa preferensi awal aplikasi telah siap
        isAppReady = true

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeMode by viewModel.themeMode.collectAsState()

            // Menentukan tema berdasarkan preferensi (Hanya Terang dan Gelap)
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                ResepediaTheme(darkTheme = isDarkTheme) {
                    AppNavigation(
                        viewModel = viewModel
                    )
                }
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
