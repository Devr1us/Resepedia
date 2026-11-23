package com.example.resepedia.data

import com.example.resepedia.model.Ingredient
import com.example.resepedia.model.MealDetail
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data class untuk menampung hasil terjemahan lengkap dari satu resep
 */
data class TranslatedMeal(
    val mealId: String,
    val title: String,
    val category: String,
    val area: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>
)

/**
 * Helper await() untuk Firebase/Play Services Task tanpa dependency play-services
 */
suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw CancellationException("Task $this dibatalkan.")
            }
            @Suppress("UNCHECKED_CAST")
            result as T
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}

/**
 * Pengelola terjemahan on-device menggunakan Google ML Kit Translate
 */
class TranslationManager {

    private val translators = ConcurrentHashMap<String, Translator>()
    private val downloadedLanguages = ConcurrentHashMap.newKeySet<String>()
    private val cache = ConcurrentHashMap<String, String>()

    companion object {
        /**
         * Memetakan kode bahasa aplikasi ke TranslateLanguage ML Kit.
         * Menambah bahasa baru cukup menambahkan satu baris mapping di sini.
         */
        fun targetFor(langCode: String): String? {
            return when (langCode.lowercase()) {
                "in", "id" -> TranslateLanguage.INDONESIAN
                // Contoh jika ingin menambah Spanyol kelak: "es" -> TranslateLanguage.SPANISH
                else -> null // "en" atau bahasa tanpa terjemahan
            }
        }

        /**
         * Kamus istilah takaran bahan makanan (Inggris -> Indonesia).
         * Menggunakan regex word-boundary agar tidak merusak kata lain.
         */
        private val INDONESIAN_MEASURE_MAP = listOf(
            "tablespoons?" to "sdm",
            "tbsp\\.?|tbs\\.?" to "sdm",
            "teaspoons?" to "sdt",
            "tsp\\.?" to "sdt",
            "cups?" to "cangkir",
            "leaves?" to "lembar",
            "cloves?" to "siung",
            "pinches|pinch" to "sejumput",
            "slices?" to "iris",
            "pieces?|pcs\\.?" to "buah",
            "packets?|pkg\\.?" to "bungkus",
            "bunches|bunch" to "ikat",
            "cans?" to "kaleng",
            "bottles?" to "botol",
            "glasses|glass" to "gelas",
            "stalks?" to "batang",
            "heads?" to "bonggol",
            "large" to "besar",
            "medium" to "sedang",
            "small" to "kecil",
            "ounces?|oz\\.?" to "ons",
            "pounds?|lbs?\\.?" to "pon"
        )
    }

    private fun cacheKey(targetLang: String, text: String): String = "$targetLang|$text"

    /**
     * Mengambil atau membuat translator untuk bahasa target dan mengunduh model jika belum ada
     */
    private suspend fun getTranslator(targetLang: String): Translator {
        val translator = translators.computeIfAbsent(targetLang) { lang ->
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(lang)
                .build()
            Translation.getClient(options)
        }

        if (!downloadedLanguages.contains(targetLang)) {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            downloadedLanguages.add(targetLang)
        }

        return translator
    }

    /**
     * Menerjemahkan kumpulan teks secara paralel.
     * Mengembalikan peta: teks asli -> teks terjemahan.
     */
    suspend fun translateTexts(texts: Collection<String>, langCode: String): Map<String, String> {
        val targetLang = targetFor(langCode) ?: return emptyMap()
        val cleanTexts = texts.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (cleanTexts.isEmpty()) return emptyMap()

        val translator = try {
            getTranslator(targetLang)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Gagal inisialisasi atau download model (misal offline saat pertama kali), kembalikan teks asli
            return cleanTexts.associateWith { it }
        }

        return coroutineScope {
            cleanTexts.map { text ->
                async {
                    val key = cacheKey(targetLang, text)
                    val cached = cache[key]
                    if (cached != null) {
                        text to cached
                    } else {
                        try {
                            val translated = translator.translate(text).await()
                            cache[key] = translated
                            text to translated
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            text to text
                        }
                    }
                }
            }.awaitAll().toMap()
        }
    }

    /**
     * Menerjemahkan detail resep lengkap (judul, kategori, negara asal, bahan, dan langkah-langkah) secara paralel.
     */
    suspend fun translateMeal(meal: MealDetail, langCode: String): TranslatedMeal {
        val mealId = meal.idMeal.orEmpty()
        val originalIngredients = meal.getIngredientsList()
        val originalSteps = meal.getNumberedInstructions()

        val targetLang = targetFor(langCode)
        if (targetLang == null) {
            return TranslatedMeal(
                mealId = mealId,
                title = meal.strMeal.orEmpty(),
                category = meal.strCategory.orEmpty(),
                area = meal.strArea.orEmpty(),
                ingredients = originalIngredients,
                steps = originalSteps
            )
        }

        val translator = try {
            getTranslator(targetLang)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return TranslatedMeal(
                mealId = mealId,
                title = meal.strMeal.orEmpty(),
                category = meal.strCategory.orEmpty(),
                area = meal.strArea.orEmpty(),
                ingredients = originalIngredients.map {
                    it.copy(measure = translateMeasure(it.measure, langCode))
                },
                steps = originalSteps
            )
        }

        return coroutineScope {
            val titleDeferred = async { translateSingle(translator, targetLang, meal.strMeal.orEmpty()) }
            val categoryDeferred = async { translateSingle(translator, targetLang, meal.strCategory.orEmpty()) }
            val areaDeferred = async { translateSingle(translator, targetLang, meal.strArea.orEmpty()) }

            val ingredientsDeferred = originalIngredients.map { ing ->
                async {
                    val translatedName = translateSingle(translator, targetLang, ing.name)
                    val translatedMeasure = translateMeasure(ing.measure, langCode)
                    Ingredient(name = translatedName, measure = translatedMeasure)
                }
            }

            val stepsDeferred = originalSteps.map { step ->
                async { translateSingle(translator, targetLang, step) }
            }

            TranslatedMeal(
                mealId = mealId,
                title = titleDeferred.await(),
                category = categoryDeferred.await(),
                area = areaDeferred.await(),
                ingredients = ingredientsDeferred.awaitAll(),
                steps = stepsDeferred.awaitAll()
            )
        }
    }

    private suspend fun translateSingle(translator: Translator, targetLang: String, text: String): String {
        if (text.isBlank()) return text
        val key = cacheKey(targetLang, text)
        val cached = cache[key]
        if (cached != null) return cached

        return try {
            val translated = translator.translate(text).await()
            cache[key] = translated
            translated
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            text
        }
    }

    /**
     * Menerjemahkan string takaran menggunakan regex word-boundary kamus khusus Indonesia.
     */
    private fun translateMeasure(measure: String, langCode: String): String {
        if (langCode != "in" && langCode != "id") return measure
        if (measure.isBlank()) return measure

        var result = measure
        for ((pattern, replacement) in INDONESIAN_MEASURE_MAP) {
            result = result.replace(Regex("(?i)\\b$pattern\\b"), replacement)
        }
        return result
    }

    /**
     * Menutup seluruh resource translator yang aktif
     */
    fun close() {
        for (translator in translators.values) {
            try {
                translator.close()
            } catch (e: Exception) {
                // Abaikan error saat close
            }
        }
        translators.clear()
        cache.clear()
        downloadedLanguages.clear()
    }
}
