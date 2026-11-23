package com.example.resepedia.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.resepedia.R
import com.example.resepedia.data.AppThemeMode
import com.example.resepedia.ui.MainViewModel
import com.example.resepedia.ui.components.AppLogo
import com.example.resepedia.ui.components.SettingsInfoRow
import com.example.resepedia.ui.components.SettingsSectionCard
import com.example.resepedia.ui.util.ShakeDetector
import com.example.resepedia.ui.util.adaptiveContentWidth

/**
 * Layar Pengaturan (Settings):
 * 1. Kelompok Tampilan: Pengaturan tema (Ikuti Sistem / Terang / Gelap) via SingleChoiceSegmentedButtonRow
 * 2. Kelompok Bahasa: Pilihan Bahasa Indonesia dan English dengan sumber kebenaran AppCompatDelegate
 * 3. Kelompok Tentang Aplikasi: Logo, versi dinamis, sumber data TheMealDB, teknologi, developer, dan catatan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentThemeMode by viewModel.themeMode.collectAsState()

    // Ambil bahasa terpilih dari AppCompatDelegate (sumber kebenaran), fallback ke ViewModel
    val currentLangTag = remember {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            appLocales[0]?.language ?: viewModel.getLanguage()
        } else {
            viewModel.getLanguage()
        }
    }
    val isIndonesianSelected = currentLangTag.equals("in", ignoreCase = true) ||
            currentLangTag.equals("id", ignoreCase = true)

    // Ambil versi aplikasi secara dinamis dari PackageManager
    val appVersionName = remember(context) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopBar Sederhana: Tombol Kembali + Judul Layar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .adaptiveContentWidth(maxWidth = 720.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Isi Konten Pengaturan (Bisa di-scroll di orientasi Portrait & Landscape, terpusat elegan di layar lebar)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .adaptiveContentWidth(maxWidth = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // ==========================================
            // 1. KELOMPOK TAMPILAN (APPEARANCE)
            // ==========================================
            SettingsSectionCard(title = stringResource(R.string.settings_appearance)) {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val themeOptions = listOf(
                    Triple(AppThemeMode.SYSTEM, stringResource(R.string.theme_system), Icons.Filled.BrightnessAuto),
                    Triple(AppThemeMode.LIGHT, stringResource(R.string.theme_light), Icons.Filled.LightMode),
                    Triple(AppThemeMode.DARK, stringResource(R.string.theme_dark), Icons.Filled.DarkMode)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    themeOptions.forEachIndexed { index, (mode, label, icon) ->
                        val isSelected = currentThemeMode == mode
                        SegmentedButton(
                            selected = isSelected,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = isSelected) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 2. KELOMPOK BAHASA (LANGUAGE)
            // ==========================================
            SettingsSectionCard(title = stringResource(R.string.settings_language)) {
                // Opsi Bahasa Indonesia
                LanguageOptionItem(
                    title = stringResource(R.string.lang_indonesian),
                    flagEmoji = "🇮🇩",
                    selected = isIndonesianSelected,
                    onClick = {
                        if (!isIndonesianSelected) {
                            viewModel.setLanguage("in")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("in")
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Opsi English
                LanguageOptionItem(
                    title = stringResource(R.string.lang_english),
                    flagEmoji = "🇬🇧",
                    selected = !isIndonesianSelected,
                    onClick = {
                        if (isIndonesianSelected) {
                            viewModel.setLanguage("en")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("en")
                            )
                        }
                    }
                )
            }

            // ==========================================
            // KELOMPOK INTERAKSI (INTERACTION) - JIKA ADA AKSELEROMETER
            // ==========================================
            val hasAccelerometer = remember { ShakeDetector.hasAccelerometer(context) }
            val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()

            if (hasAccelerometer) {
                SettingsSectionCard(title = stringResource(R.string.settings_section_interaction)) {
                    SettingsInfoRow(
                        icon = Icons.Outlined.Vibration,
                        title = stringResource(R.string.settings_shake_title),
                        subtitle = stringResource(R.string.settings_shake_desc),
                        trailingContent = {
                            Switch(
                                checked = isShakeEnabled,
                                onCheckedChange = { viewModel.setShakeEnabled(it) }
                            )
                        }
                    )
                }
            }

            // ==========================================
            // 3. KELOMPOK TENTANG APLIKASI (ABOUT)
            // ==========================================
            SettingsSectionCard(title = stringResource(R.string.settings_about)) {
                // Header Profil Aplikasi: Logo, Nama, Tagline, Versi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    AppLogo(size = 52.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_version, appVersionName),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Baris Sumber Data (TheMealDB) - Klik untuk membuka situs
                SettingsInfoRow(
                    icon = Icons.Outlined.Public,
                    title = stringResource(R.string.settings_data_source),
                    subtitle = stringResource(R.string.settings_data_source_note),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themealdb.com"))
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // Abaikan bila tidak ada aplikasi browser
                        }
                    }
                )

                // Baris Teknologi
                SettingsInfoRow(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.settings_tech_stack_title),
                    subtitle = stringResource(R.string.settings_tech_stack)
                )

                // Baris Pengembang
                SettingsInfoRow(
                    icon = Icons.Outlined.Person,
                    title = stringResource(R.string.settings_developer),
                    subtitle = stringResource(R.string.settings_developer_name)
                )

                // Baris Catatan / Disclaimer
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_disclaimer),
                    subtitle = null
                )

                // Baris Putar Ulang Onboarding (Panduan Pengantar Aplikasi)
                SettingsInfoRow(
                    icon = Icons.Outlined.AutoStories,
                    title = stringResource(R.string.settings_replay_onboarding),
                    subtitle = stringResource(R.string.settings_replay_onboarding_desc),
                    onClick = {
                        viewModel.setOnboardingDone(false)
                        onNavigateToOnboarding()
                    }
                )
            }
        }
    }
}
}

/**
 * Item pilihan bahasa dengan radio button, emoji bendera, dan teks label.
 */
@Composable
private fun LanguageOptionItem(
    title: String,
    flagEmoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = flagEmoji,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
