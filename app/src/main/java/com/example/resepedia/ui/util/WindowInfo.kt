package com.example.resepedia.ui.util

import android.content.res.Configuration
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal untuk menyediakan WindowSizeClass di seluruh pohon komposisi.
 * Bernilai null secara default untuk mendukung lingkungan Compose @Preview tanpa crash.
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass?> { null }

/**
 * Multi-device Preview annotation:
 * 1. Phone Portrait (411 x 891 dp)
 * 2. Phone Landscape (891 x 411 dp)
 * 3. Tablet 10 Inch (1280 x 800 dp)
 * 4. Aksesibilitas Font Scale 1.3x
 */
@Preview(name = "1. Phone Portrait", device = "spec:width=411dp,height=891dp", showBackground = true)
@Preview(name = "2. Phone Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true)
@Preview(name = "3. Tablet 10 inch", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true)
@Preview(name = "4. Font Scale 1.3x", fontScale = 1.3f, showBackground = true)
annotation class ResponsiveDevicePreviews

/**
 * Data class helper untuk membaca status ukuran layar dan orientasi secara mudah dan responsif.
 */
data class AppWindowInfo(
    val isLandscape: Boolean,
    val isCompactHeight: Boolean,
    val isExpandedWidth: Boolean,
    val isMediumOrExpandedWidth: Boolean,
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
)

/**
 * Mendapatkan AppWindowInfo dari LocalWindowSizeClass dan LocalConfiguration saat ini.
 * Memperhitungkan orientasi serta ukuran jendela split-screen / tablet / foldable.
 * Dilengkapi fallback otomatis ke konfigurasi layar saat dijalankan di Compose Previews.
 */
@Composable
fun rememberWindowInfo(): AppWindowInfo {
    val windowSizeClass = LocalWindowSizeClass.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isCompactHeight = windowSizeClass?.heightSizeClass == WindowHeightSizeClass.Compact || configuration.screenHeightDp < 480
    val isExpandedWidth = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded || configuration.screenWidthDp >= 840
    val isMediumOrExpandedWidth = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact || configuration.screenWidthDp >= 600

    val widthSize = windowSizeClass?.widthSizeClass ?: when {
        configuration.screenWidthDp >= 840 -> WindowWidthSizeClass.Expanded
        configuration.screenWidthDp >= 600 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Compact
    }

    val heightSize = windowSizeClass?.heightSizeClass ?: when {
        configuration.screenHeightDp < 480 -> WindowHeightSizeClass.Compact
        configuration.screenHeightDp < 900 -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Expanded
    }

    return AppWindowInfo(
        isLandscape = isLandscape,
        isCompactHeight = isCompactHeight,
        isExpandedWidth = isExpandedWidth,
        isMediumOrExpandedWidth = isMediumOrExpandedWidth,
        widthSizeClass = widthSize,
        heightSizeClass = heightSize
    )
}

/**
 * Helper tunggal untuk menentukan GridCells secara responsif di seluruh aplikasi.
 * Menggunakan Adaptive dengan minSize 160.dp sehingga:
 * - Layar ponsel portrait (lebar ~360-400dp): otomatis 2 kolom
 * - Layar ponsel landscape / tablet sedang (lebar ~600-840dp): otomatis 3-4 kolom
 * - Tablet layar lebar (lebar > 840dp): otomatis 4-6 kolom
 * Menghindari kartu resep/area menjadi terlalu lebar di orientasi horizontal.
 */
fun responsiveGridCells(minSize: Dp = 160.dp): GridCells {
    return GridCells.Adaptive(minSize = minSize)
}

/**
 * Helper untuk menghitung jumlah kolom responsif integer berdasarkan WindowWidthSizeClass:
 * - Compact width: 2 kolom (portrait) atau 3 kolom (landscape ponsel)
 * - Medium width: 3 kolom
 * - Expanded width: 4 kolom atau lebih
 */
@Composable
fun rememberResponsiveGridColumns(): Int {
    val windowInfo = rememberWindowInfo()
    return when (windowInfo.widthSizeClass) {
        WindowWidthSizeClass.Compact -> if (windowInfo.isLandscape) 3 else 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
    }
}

/**
 * Helper pembatas lebar konten tekstual/form pada layar lebar (Settings, Onboarding, Dialogs, dll.)
 * agar baris teks tidak memanjang secara berlebihan di tablet atau layar horizontal.
 */
fun Modifier.adaptiveContentWidth(maxWidth: Dp = 720.dp): Modifier {
    return this.widthIn(max = maxWidth)
}
