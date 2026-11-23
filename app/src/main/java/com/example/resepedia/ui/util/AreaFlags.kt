package com.example.resepedia.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.resepedia.R

object AreaFlags {

    /**
     * Pemetaan nama negara/area dari TheMealDB ke emoji bendera masing-masing negara.
     * Menggunakan emoji Unicode standar tanpa memerlukan aset gambar tambahan.
     */
    private val AREA_FLAG_MAP: Map<String, String> = mapOf(
        "American" to "🇺🇸",
        "British" to "🇬🇧",
        "Canadian" to "🇨🇦",
        "Chinese" to "🇨🇳",
        "Croatian" to "🇭🇷",
        "Dutch" to "🇳🇱",
        "Egyptian" to "🇪🇬",
        "Filipino" to "🇵🇭",
        "French" to "🇫🇷",
        "Greek" to "🇬🇷",
        "Indian" to "🇮🇳",
        "Irish" to "🇮🇪",
        "Italian" to "🇮🇹",
        "Jamaican" to "🇯🇲",
        "Japanese" to "🇯🇵",
        "Kenyan" to "🇰🇪",
        "Malaysian" to "🇲🇾",
        "Mexican" to "🇲🇽",
        "Moroccan" to "🇲🇦",
        "Polish" to "🇵🇱",
        "Portuguese" to "🇵🇹",
        "Russian" to "🇷🇺",
        "Spanish" to "🇪🇸",
        "Thai" to "🇹🇭",
        "Tunisian" to "🇹🇳",
        "Turkish" to "🇹🇷",
        "Ukrainian" to "🇺🇦",
        "Uruguayan" to "🇺🇾",
        "Vietnamese" to "🇻🇳",
        "Indonesian" to "🇮🇩",
        "Norwegian" to "🇳🇴",
        "Saudi Arabian" to "🇸🇦",
        "Syrian" to "🇸🇾",
        "Algerian" to "🇩🇿",
        "Argentinian" to "🇦🇷",
        "Australian" to "🇦🇺"
    )

    private const val DEFAULT_FLAG: String = "🍽️"

    /**
     * Mengambil emoji bendera berdasarkan nama area.
     * Mengembalikan fallback emoji sendok garpu (🍽️) bila area belum terdaftar.
     */
    fun getFlag(area: String): String {
        return AREA_FLAG_MAP[area.trim()] ?: DEFAULT_FLAG
    }

    /**
     * Pemetaan nama area ke string resource id untuk dukungan multi-bahasa.
     */
    private val AREA_STRING_RES_MAP: Map<String, Int> = mapOf(
        "American" to R.string.area_american,
        "British" to R.string.area_british,
        "Canadian" to R.string.area_canadian,
        "Chinese" to R.string.area_chinese,
        "Croatian" to R.string.area_croatian,
        "Dutch" to R.string.area_dutch,
        "Egyptian" to R.string.area_egyptian,
        "Filipino" to R.string.area_filipino,
        "French" to R.string.area_french,
        "Greek" to R.string.area_greek,
        "Indian" to R.string.area_indian,
        "Irish" to R.string.area_irish,
        "Italian" to R.string.area_italian,
        "Jamaican" to R.string.area_jamaican,
        "Japanese" to R.string.area_japanese,
        "Kenyan" to R.string.area_kenyan,
        "Malaysian" to R.string.area_malaysian,
        "Mexican" to R.string.area_mexican,
        "Moroccan" to R.string.area_moroccan,
        "Polish" to R.string.area_polish,
        "Portuguese" to R.string.area_portuguese,
        "Russian" to R.string.area_russian,
        "Spanish" to R.string.area_spanish,
        "Thai" to R.string.area_thai,
        "Tunisian" to R.string.area_tunisian,
        "Turkish" to R.string.area_turkish,
        "Ukrainian" to R.string.area_ukrainian,
        "Uruguayan" to R.string.area_uruguayan,
        "Vietnamese" to R.string.area_vietnamese,
        "Indonesian" to R.string.area_indonesian,
        "Norwegian" to R.string.area_norwegian,
        "Saudi Arabian" to R.string.area_saudi_arabian,
        "Syrian" to R.string.area_syrian,
        "Algerian" to R.string.area_algerian,
        "Argentinian" to R.string.area_argentinian,
        "Australian" to R.string.area_australian
    )

    fun getStringResId(area: String): Int? {
        return AREA_STRING_RES_MAP[area.trim()]
    }
}

/**
 * Mengambil emoji bendera untuk area yang diberikan.
 */
fun getAreaFlag(area: String): String = AreaFlags.getFlag(area)

/**
 * Menghasilkan nama tampilan area yang terlokalisasi sesuai bahasa aktif di aplikasi.
 * Jika key string tidak tersedia, secara aman mengembalikan nama asli dari API.
 */
@Composable
fun areaDisplayName(area: String): String {
    val resId = AreaFlags.getStringResId(area)
    return if (resId != null) {
        stringResource(resId)
    } else {
        area
    }
}
