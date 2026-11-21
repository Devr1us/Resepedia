# 🍲 Resepedia — Ensiklopedia Resep Masakan Modern

Aplikasi Android katalog dan ensiklopedia resep masakan berbasis **100% Kotlin** dan **Jetpack Compose (Material 3)** dengan data dinamis langsung dari **TheMealDB API**. Aplikasi ini dibuat untuk memenuhi tugas **"Membuat Aplikasi Android yang Menerapkan Jetpack Compose dan API"**.

---

## 📋 Tabel Pemetaan Ketentuan Tugas

Berikut adalah pemetaan pemenuhan setiap butir kriteria penilaian tugas:

| No | Ketentuan Tugas | Implementasi di Resepedia | Status |
|:--:|:---|:---|:--:|
| **1** | **Memakai Jetpack Compose sebagai toolkit UI** | 100% antarmuka dibangun deklaratif menggunakan Jetpack Compose & Material 3. Tanpa file XML layout, tanpa Fragment, tanpa ViewBinding/DataBinding. | ✅ **100% Terpenuhi** |
| **2** | **Dashboard / Halaman Utama didesain semenarik mungkin** | Tampilan bernuansa hangat (*warm orange* & *fresh green*). Dilengkapi animasi halus, header identitas modern, search bar membulat (*rounded pill*), Hero Card besar *"Resep Hari Ini"* dengan gradien, horizontal scrolling kategori, dan kartu resep dinamis. | ✅ **100% Terpenuhi** (Nilai 30%) |
| **3** | **Menampilkan data dari API** | Integrasi penuh dengan **TheMealDB REST API** menggunakan Retrofit 2 + OkHttp + Gson: daftar kategori, resep per kategori, pencarian resep, detail bahan & langkah memasak, serta resep acak harian. | ✅ **100% Terpenuhi** |
| **4** | **Bisa dijalankan dalam Multi Bahasa** | Mendukung **Bahasa Indonesia (`res/values-in/`)** dan **English (`res/values/`)**. Semua string teks UI terpusat di `strings.xml`. Pilihan bahasa dapat diganti langsung via dialog dan tersimpan secara persisten. | ✅ **100% Terpenuhi** (Nilai 15%) |
| **5** | **Data ditampilkan dalam bentuk CARD yang dibungkus list ala RecyclerView** | Menggunakan komponen kartu resep kustom `RecipeCard` berpenampilan modern (sudut membulat 20dp, elevasi lembut, rasio foto makanan fokus, tombol bookmark/favorit interaktif) yang ditampilkan menggunakan `LazyVerticalGrid(columns = GridCells.Fixed(2))`. | ✅ **100% Terpenuhi** (Nilai 25%) |
| **6** | **Desain bisa dipakai di mode Not Night (terang) DAN Night (gelap)** | Mengimplementasikan palet warna kontras tinggi khusus untuk *Light Theme* (krem hangat & oranye) dan *Dark Theme* (hitam pekat `#121212` & aksen oranye). Teks dan elemen visual tetap terbaca jelas di kedua mode. Pengguna dapat memilih: Ikuti Sistem, Terang, atau Gelap. | ✅ **100% Terpenuhi** |
| **7** | **Saat layar dirotasi, desain TIDAK BERUBAH dan masih bisa di-scroll** | Orientasi bebas tanpa penguncian (`unlocked`). Struktur 2 kolom (`GridCells.Fixed(2)`) tetap dipertahankan baik pada mode *Portrait* maupun *Landscape*. Seluruh halaman di-wrap dalam satu scroll container sehingga tetap bisa di-scroll lancar tanpa ada elemen terpotong. State tersimpan aman via ViewModel & `rememberSaveable`. | ✅ **100% Terpenuhi** |

---

## 💡 Mengapa `LazyVerticalGrid` Adalah Pengganti Setara `RecyclerView` di Jetpack Compose?

Pada paradigma Android View klasik (XML), menampilkan daftar card dalam format grid membutuhkan:
1. `RecyclerView` di layout XML.
2. `GridLayoutManager` dengan span count 2.
3. Class `RecyclerView.Adapter` dan `ViewHolder`.
4. File layout XML terpisah untuk item card.
5. `DiffUtil.ItemCallback` untuk pembaruan list secara efisien.

Di **Jetpack Compose**, pendekatan imperatif tersebut digantikan sepenuhnya oleh **`LazyVerticalGrid`**:
- **Dukungan Lazy Loading Otomatis**: Hanya item yang terlihat di layar yang dikomposisi dan dirender ke memori, identik dengan mekanisme *view recycling* pada `RecyclerView`.
- **Pengaturan Kolom Sederhana & Kokoh**: Menggunakan parameter `columns = GridCells.Fixed(2)` untuk menjaga konsistensi 2 kolom card di portrait maupun landscape.
- **Efisiensi Diffing dengan Parameter `key`**: Setiap item diberi `key = { it.idMeal }` sehingga recomposition hanya terjadi pada item yang datanya berubah.
- **Span Penuh untuk Header**: Menggunakan `span = { GridItemSpan(maxLineSpan) }` untuk bagian header, search bar, hero card, dan kategori sehingga seluruh layar bersatu dalam satu *scrollable surface* tanpa error *nested scrolling*.

---

## 📱 Tangkapan Layar (Screenshots)

| Mode Terang (Light Mode) | Mode Gelap (Dark Mode) |
|:---:|:---:|
| *(Screenshot Dashboard Light)* | *(Screenshot Dashboard Dark)* |
| `docs/screenshots/dashboard_light.png` | `docs/screenshots/dashboard_dark.png` |

| Tampilan Landscape (Rotasi Layar) | Multi-Bahasa (English & Indonesia) |
|:---:|:---:|
| *(Screenshot Mode Landscape 2 Kolom)* | *(Screenshot Dialog Bahasa)* |
| `docs/screenshots/landscape_scroll.png` | `docs/screenshots/language_dialog.png` |

| Detail Resep & Bahan | Layar Favorit (Tersimpan) |
|:---:|:---:|
| *(Screenshot Detail Resep + YouTube)* | *(Screenshot Resep Favorit)* |
| `docs/screenshots/detail_recipe.png` | `docs/screenshots/favorites_screen.png` |

---

## 🛠️ Arsitektur & Teknologi

### Arsitektur: **MVVM (Model - View - ViewModel)**
- **UI Layer (View)**: Jetpack Compose, Material 3, Navigation Compose.
- **ViewModel Layer**: `AndroidViewModel` dengan `StateFlow` dan `viewModelScope` coroutine untuk reaktivitas UI yang stabil dan *lifecycle-aware*.
- **Data Layer**: `RecipeRepository`, `PreferencesManager` (SharedPreferences), `ApiClient` (Retrofit).
- **State Management**: `sealed interface UiState<T>` (`Loading`, `Success`, `Error`) untuk penanganan status UI yang bersih.

### Pustaka & Dependensi (100% Versi Stabil)
- **UI Toolkit**: Jetpack Compose (BOM 2026.02.01) + Material 3.
- **Networking**: Retrofit 2.11.0 + OkHttp Logging Interceptor 4.12.0.
- **JSON Converter**: Converter Gson 2.11.0.
- **Image Loading**: Coil 3.0.4 (`coil-compose` + `coil-network-okhttp`).
- **Navigation**: Jetpack Navigation Compose 2.8.5.
- **Icons**: Material Icons Extended.
- **Multi-Language Support**: `androidx.appcompat:appcompat:1.7.0` + `AppCompatDelegate.setApplicationLocales()`.

---

## 🌐 Endpoint API yang Digunakan (TheMealDB)

Base URL: `https://www.themealdb.com/api/json/v1/1/`

| Endpoint | Kegunaan di Resepedia |
|:---|:---|
| `random.php` | Mengambil data resep acak untuk Hero Card *"Resep Hari Ini"* di Dashboard. |
| `categories.php` | Mengambil daftar semua kategori masakan beserta gambar thumbnail. |
| `filter.php?c={category}` | Mengambil daftar kartu resep sesuai kategori yang sedang dipilih (default: Chicken). |
| `search.php?s={query}` | Melakukan pencarian resep secara real-time berdasarkan kata kunci nama resep. |
| `lookup.php?i={id}` | Mengambil detail lengkap resep (bahan, takaran, langkah memasak, dan tautan tutorial YouTube). |

---

## 🚀 Cara Menjalankan Proyek (How to Run)

1. **Clone repositori ini**:
   ```bash
   git clone https://github.com/Devr1us/Resepedia.git
   cd Resepedia
   ```
2. **Buka di Android Studio**:
   - Buka Android Studio (disarankan versi Hedgehog / Iguana / Koala / Ladybug / Rabbit).
   - Pilih **Open** dan arahkan ke folder proyek `Resepedia`.
3. **Sinkronisasi Gradle**:
   - Tunggu Android Studio menyelesaikan *Gradle Sync*.
   - Pastikan JDK yang digunakan adalah JDK 11 atau lebih baru (bawaan Android Studio JBR).
4. **Jalankan Aplikasi**:
   - Sambungkan HP fisik (aktifkan *USB Debugging*) atau jalankan Android Emulator.
   - Klik tombol **Run 'app'** (Shift + F10).

---

## 👤 Kontributor

- **Pengembang**: devr1us
- **Email**: deriusaditya23@gmail.com
- **Repositori**: [https://github.com/Devr1us/Resepedia](https://github.com/Devr1us/Resepedia)
