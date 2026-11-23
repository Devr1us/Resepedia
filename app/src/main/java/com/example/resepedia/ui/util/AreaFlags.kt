package com.example.resepedia.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.resepedia.R
import java.util.Locale

object AreaFlags {

    const val FALLBACK_FLAG: String = "🌍"

    /**
     * Peta komprehensif demonym, nama area, dan variasi ejaan (lowercase & trimmed)
     * ke kode ISO 3166-1 alpha-2 untuk seluruh negara di dunia (~195+).
     */
    private val AREA_TO_ISO_MAP: Map<String, String> = mapOf(
        // TheMealDB & Demonyms umum
        "afghan" to "AF",
        "albanian" to "AL",
        "algerian" to "DZ",
        "american" to "US",
        "andorran" to "AD",
        "angolan" to "AO",
        "antiguan, barbudan" to "AG",
        "antiguan" to "AG",
        "barbudan" to "AG",
        "argentine" to "AR",
        "argentinian" to "AR",
        "armenian" to "AM",
        "aruban" to "AW",
        "australian" to "AU",
        "austrian" to "AT",
        "azerbaijani" to "AZ",
        "bahamian" to "BS",
        "bahraini" to "BH",
        "bangladeshi" to "BD",
        "barbadian" to "BB",
        "belarusian" to "BY",
        "belgian" to "BE",
        "belizean" to "BZ",
        "beninese" to "BJ",
        "bermudian" to "BM",
        "bhutanese" to "BT",
        "bolivian" to "BO",
        "bosnian, herzegovinian" to "BA",
        "bosnian" to "BA",
        "herzegovinian" to "BA",
        "motswana" to "BW",
        "botswanan" to "BW",
        "brazilian" to "BR",
        "british" to "GB",
        "english" to "GB",
        "scottish" to "GB",
        "welsh" to "GB",
        "bruneian" to "BN",
        "bulgarian" to "BG",
        "burkinabe" to "BF",
        "burundian" to "BI",
        "cambodian" to "KH",
        "cameroonian" to "CM",
        "canadian" to "CA",
        "cape verdian" to "CV",
        "cape verdean" to "CV",
        "caymanian" to "KY",
        "central african" to "CF",
        "chadian" to "TD",
        "chilean" to "CL",
        "chinese" to "CN",
        "colombian" to "CO",
        "congolese" to "CD",
        "costa rican" to "CR",
        "croatian" to "HR",
        "cuban" to "CU",
        "cypriot" to "CY",
        "czech" to "CZ",
        "danish" to "DK",
        "djibouti" to "DJ",
        "djiboutian" to "DJ",
        "dominican" to "DO",
        "dutch" to "NL",
        "ecuadorean" to "EC",
        "ecuadorian" to "EC",
        "egyptian" to "EG",
        "emirati" to "AE",
        "equatorial guinean" to "GQ",
        "eritrean" to "ER",
        "estonian" to "EE",
        "ethiopian" to "ET",
        "faroese" to "FO",
        "fijian" to "FJ",
        "filipino" to "PH",
        "finnish" to "FI",
        "french" to "FR",
        "gabonese" to "GA",
        "gambian" to "GM",
        "georgian" to "GE",
        "german" to "DE",
        "ghanaian" to "GH",
        "gibraltar" to "GI",
        "greek" to "GR",
        "greenlandic" to "GL",
        "grenadian" to "GD",
        "guadeloupian" to "GP",
        "guamanian" to "GU",
        "guatemalan" to "GT",
        "channel islander" to "JE",
        "guinean" to "GN",
        "guinea-bissauan" to "GW",
        "guyanese" to "GY",
        "haitian" to "HT",
        "honduran" to "HN",
        "hong konger" to "HK",
        "hungarian" to "HU",
        "icelander" to "IS",
        "icelandic" to "IS",
        "indian" to "IN",
        "indonesian" to "ID",
        "iranian" to "IR",
        "iraqi" to "IQ",
        "irish" to "IE",
        "israeli" to "IL",
        "italian" to "IT",
        "ivorian" to "CI",
        "jamaican" to "JM",
        "japanese" to "JP",
        "jordanian" to "JO",
        "kazakhstani" to "KZ",
        "kazakh" to "KZ",
        "kenyan" to "KE",
        "kirghiz" to "KG",
        "kyrgyz" to "KG",
        "kosovar" to "XK",
        "kuwaiti" to "KW",
        "laotian" to "LA",
        "lao" to "LA",
        "latvian" to "LV",
        "lebanese" to "LB",
        "mosotho" to "LS",
        "liberian" to "LR",
        "libyan" to "LY",
        "liechtensteiner" to "LI",
        "lithuanian" to "LT",
        "luxembourger" to "LU",
        "macedonian" to "MK",
        "malagasy" to "MG",
        "malawian" to "MW",
        "malaysian" to "MY",
        "maldivan" to "MV",
        "maldivian" to "MV",
        "malian" to "ML",
        "maltese" to "MT",
        "mauritian" to "MU",
        "mexican" to "MX",
        "moldovan" to "MD",
        "mongolian" to "MN",
        "montenegrin" to "ME",
        "moroccan" to "MA",
        "mozambican" to "MZ",
        "burmese" to "MM",
        "namibian" to "NA",
        "nepalese" to "NP",
        "nepali" to "NP",
        "new zealander" to "NZ",
        "nicaraguan" to "NI",
        "nigerien" to "NE",
        "nigerian" to "NG",
        "north korean" to "KP",
        "norwegian" to "NO",
        "omani" to "OM",
        "pakistani" to "PK",
        "palestinian" to "PS",
        "panamanian" to "PA",
        "papua new guinean" to "PG",
        "paraguayan" to "PY",
        "peruvian" to "PE",
        "polish" to "PL",
        "portuguese" to "PT",
        "puerto rican" to "PR",
        "qatari" to "QA",
        "romanian" to "RO",
        "russian" to "RU",
        "rwandan" to "RW",
        "saint lucian" to "LC",
        "salvadoran" to "SV",
        "sammarinese" to "SM",
        "samoan" to "WS",
        "saudi arabian" to "SA",
        "saudi" to "SA",
        "senegalese" to "SN",
        "serbian" to "RS",
        "seychellois" to "SC",
        "sierra leonean" to "SL",
        "singaporean" to "SG",
        "slovak" to "SK",
        "slovakian" to "SK",
        "slovene" to "SI",
        "slovenian" to "SI",
        "solomon islander" to "SB",
        "somali" to "SO",
        "south african" to "ZA",
        "south korean" to "KR",
        "korean" to "KR",
        "south sudanese" to "SS",
        "spanish" to "ES",
        "sri lankan" to "LK",
        "sudanese" to "SD",
        "surinamer" to "SR",
        "surinamese" to "SR",
        "swedish" to "SE",
        "swiss" to "CH",
        "syrian" to "SY",
        "tadzhik" to "TJ",
        "tajik" to "TJ",
        "taiwanese" to "TW",
        "tanzanian" to "TZ",
        "thai" to "TH",
        "togolese" to "TG",
        "tongan" to "TO",
        "trinidadian" to "TT",
        "tunisian" to "TN",
        "turkish" to "TR",
        "turkmen" to "TM",
        "tuvaluan" to "TV",
        "ugandan" to "UG",
        "ukrainian" to "UA",
        "uruguayan" to "UY",
        "uzbekistani" to "UZ",
        "uzbek" to "UZ",
        "ni-vanuatu" to "VU",
        "vanuatuan" to "VU",
        "venezuelan" to "VE",
        "venezulan" to "VE", // Typo historis TheMealDB
        "vietnamese" to "VN",
        "yemeni" to "YE",
        "zambian" to "ZM",
        "zimbabwean" to "ZW",

        // Nama negara langsung & sinonim umum
        "afghanistan" to "AF",
        "albania" to "AL",
        "algeria" to "DZ",
        "andorra" to "AD",
        "angola" to "AO",
        "antigua and barbuda" to "AG",
        "argentina" to "AR",
        "armenia" to "AM",
        "aruba" to "AW",
        "australia" to "AU",
        "austria" to "AT",
        "azerbaijan" to "AZ",
        "bahamas" to "BS",
        "bahrain" to "BH",
        "bangladesh" to "BD",
        "barbados" to "BB",
        "belarus" to "BY",
        "belgium" to "BE",
        "belize" to "BZ",
        "benin" to "BJ",
        "bermuda" to "BM",
        "bhutan" to "BT",
        "bolivia" to "BO",
        "bosnia and herzegovina" to "BA",
        "botswana" to "BW",
        "brazil" to "BR",
        "brunei" to "BN",
        "bulgaria" to "BG",
        "burkina faso" to "BF",
        "burundi" to "BI",
        "cabo verde" to "CV",
        "cape verde" to "CV",
        "cambodia" to "KH",
        "cameroon" to "CM",
        "canada" to "CA",
        "cayman islands" to "KY",
        "central african republic" to "CF",
        "chad" to "TD",
        "chile" to "CL",
        "china" to "CN",
        "colombia" to "CO",
        "comoros" to "KM",
        "dr congo" to "CD",
        "republic of the congo" to "CG",
        "congo" to "CD",
        "costa rica" to "CR",
        "croatia" to "HR",
        "cuba" to "CU",
        "cyprus" to "CY",
        "czechia" to "CZ",
        "czech republic" to "CZ",
        "denmark" to "DK",
        "dominica" to "DM",
        "dominican republic" to "DO",
        "east timor" to "TL",
        "timor-leste" to "TL",
        "ecuador" to "EC",
        "egypt" to "EG",
        "el salvador" to "SV",
        "equatorial guinea" to "GQ",
        "eritrea" to "ER",
        "estonia" to "EE",
        "eswatini" to "SZ",
        "swaziland" to "SZ",
        "ethiopia" to "ET",
        "faroe islands" to "FO",
        "fiji" to "FJ",
        "finland" to "FI",
        "france" to "FR",
        "gabon" to "GA",
        "gambia" to "GM",
        "georgia" to "GE",
        "germany" to "DE",
        "ghana" to "GH",
        "greece" to "GR",
        "grenada" to "GD",
        "guatemala" to "GT",
        "guinea" to "GN",
        "guinea-bissau" to "GW",
        "guyana" to "GY",
        "haiti" to "HT",
        "honduras" to "HN",
        "hong kong" to "HK",
        "hungary" to "HU",
        "iceland" to "IS",
        "india" to "IN",
        "indonesia" to "ID",
        "iran" to "IR",
        "iraq" to "IQ",
        "ireland" to "IE",
        "israel" to "IL",
        "italy" to "IT",
        "ivory coast" to "CI",
        "cote d'ivoire" to "CI",
        "jamaica" to "JM",
        "japan" to "JP",
        "jordan" to "JO",
        "kazakhstan" to "KZ",
        "kenya" to "KE",
        "kiribati" to "KI",
        "kosovo" to "XK",
        "kuwait" to "KW",
        "kyrgyzstan" to "KG",
        "laos" to "LA",
        "latvia" to "LV",
        "lebanon" to "LB",
        "lesotho" to "LS",
        "liberia" to "LR",
        "libya" to "LY",
        "liechtenstein" to "LI",
        "lithuania" to "LT",
        "luxembourg" to "LU",
        "madagascar" to "MG",
        "malawi" to "MW",
        "malaysia" to "MY",
        "maldives" to "MV",
        "mali" to "ML",
        "malta" to "MT",
        "marshall islands" to "MH",
        "mauritania" to "MR",
        "mauritius" to "MU",
        "mexico" to "MX",
        "micronesia" to "FM",
        "moldova" to "MD",
        "monaco" to "MC",
        "mongolia" to "MN",
        "montenegro" to "ME",
        "morocco" to "MA",
        "mozambique" to "MZ",
        "myanmar" to "MM",
        "namibia" to "NA",
        "nauru" to "NR",
        "nepal" to "NP",
        "netherlands" to "NL",
        "holland" to "NL",
        "new zealand" to "NZ",
        "nicaragua" to "NI",
        "niger" to "NE",
        "nigeria" to "NG",
        "north korea" to "KP",
        "north macedonia" to "MK",
        "norway" to "NO",
        "oman" to "OM",
        "pakistan" to "PK",
        "palau" to "PW",
        "palestine" to "PS",
        "panama" to "PA",
        "papua new guinea" to "PG",
        "paraguay" to "PY",
        "peru" to "PE",
        "philippines" to "PH",
        "poland" to "PL",
        "portugal" to "PT",
        "puerto rico" to "PR",
        "qatar" to "QA",
        "romania" to "RO",
        "russia" to "RU",
        "rwanda" to "RW",
        "saint kitts and nevis" to "KN",
        "saint lucia" to "LC",
        "saint vincent and the grenadines" to "VC",
        "samoa" to "WS",
        "san marino" to "SM",
        "sao tome and principe" to "ST",
        "saudi arabia" to "SA",
        "senegal" to "SN",
        "serbia" to "RS",
        "seychelles" to "SC",
        "sierra leone" to "SL",
        "singapore" to "SG",
        "slovakia" to "SK",
        "slovenia" to "SI",
        "solomon islands" to "SB",
        "somalia" to "SO",
        "south africa" to "ZA",
        "south korea" to "KR",
        "korea" to "KR",
        "south sudan" to "SS",
        "spain" to "ES",
        "sri lanka" to "LK",
        "sudan" to "SD",
        "suriname" to "SR",
        "sweden" to "SE",
        "switzerland" to "CH",
        "syria" to "SY",
        "taiwan" to "TW",
        "tajikistan" to "TJ",
        "tanzania" to "TZ",
        "thailand" to "TH",
        "togo" to "TG",
        "tonga" to "TO",
        "trinidad and tobago" to "TT",
        "tunisia" to "TN",
        "turkey" to "TR",
        "turkiye" to "TR",
        "turkmenistan" to "TM",
        "tuvalu" to "TV",
        "uganda" to "UG",
        "ukraine" to "UA",
        "united arab emirates" to "AE",
        "uae" to "AE",
        "united kingdom" to "GB",
        "uk" to "GB",
        "great britain" to "GB",
        "united states" to "US",
        "usa" to "US",
        "uruguay" to "UY",
        "uzbekistan" to "UZ",
        "vanuatu" to "VU",
        "vatican city" to "VA",
        "venezuela" to "VE",
        "vietnam" to "VN",
        "yemen" to "YE",
        "zambia" to "ZM",
        "zimbabwe" to "ZW"
    )

    /**
     * Cache daftar negara dari sistem Java Locale untuk pencocokan dinamis cadangan.
     */
    private val localeCountriesMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (iso in Locale.getISOCountries()) {
            val locale = Locale.Builder().setRegion(iso).build()
            val countryName = locale.getDisplayCountry(Locale.ENGLISH).trim().lowercase(Locale.ROOT)
            if (countryName.isNotBlank()) {
                map[countryName] = iso
            }
        }
        map
    }

    /**
     * Mengambil kode ISO 3166-1 alpha-2 (huruf besar) dari nama area atau nama negara.
     * Mengembalikan null bila benar-benar tidak dikenali atau berupa "Unknown".
     */
    fun getIsoCode(area: String?): String? {
        if (area.isNullOrBlank()) return null
        val clean = area.trim().lowercase(Locale.ROOT)
        if (clean == "unknown") return null

        // 1. Cek pemetaan langsung
        AREA_TO_ISO_MAP[clean]?.let { return it }

        // 2. Coba cocokkan dengan nama negara resmi via Java Locale
        localeCountriesMap[clean]?.let { return it }

        // 3. Pencocokan prefiks/kata kunci negara secara hati-hati
        for ((country, iso) in localeCountriesMap) {
            if (clean.startsWith(country) || country.startsWith(clean)) {
                return iso
            }
        }

        return null
    }

    /**
     * Mengonversi 2 huruf kode negara ISO alpha-2 ke emoji bendera (Unicode Regional Indicator).
     * Contoh: "ID" -> 🇮🇩, "US" -> 🇺🇸, "GB" -> 🇬🇧.
     */
    fun isoCodeToFlagEmoji(isoCode: String?): String {
        if (isoCode == null || isoCode.length != 2) return FALLBACK_FLAG
        val first = Character.toUpperCase(isoCode[0])
        val second = Character.toUpperCase(isoCode[1])
        if (first !in 'A'..'Z' || second !in 'A'..'Z') return FALLBACK_FLAG

        val firstCodePoint = 0x1F1E6 + (first - 'A')
        val secondCodePoint = 0x1F1E6 + (second - 'A')
        return String(Character.toChars(firstCodePoint)) + String(Character.toChars(secondCodePoint))
    }

    /**
     * Mengambil emoji bendera berdasarkan nama area / negara.
     * Mengembalikan emoji bendera negara terkait, atau 🌍 bila tidak ditemukan / "Unknown".
     */
    fun getFlag(area: String): String {
        val iso = getIsoCode(area)
        return if (iso != null) {
            isoCodeToFlagEmoji(iso)
        } else {
            FALLBACK_FLAG
        }
    }

    /**
     * Mengambil URL aset gambar bendera berkualitas tinggi dari FlagCDN.
     * Contoh: "https://flagcdn.com/w80/id.png"
     */
    fun getFlagUrl(area: String): String? {
        val iso = getIsoCode(area) ?: return null
        return "https://flagcdn.com/w80/${iso.lowercase(Locale.ROOT)}.png"
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

    /**
     * Mengambil nama tampilan area terlokalisasi secara non-composable memakai Context.
     */
    fun getDisplayName(area: String, context: android.content.Context): String {
        val resId = getStringResId(area)
        return if (resId != null) {
            context.getString(resId)
        } else {
            area
        }
    }
}

/**
 * Mengambil emoji bendera untuk area yang diberikan.
 */
fun getAreaFlag(area: String): String = AreaFlags.getFlag(area)

/**
 * Mengambil nama tampilan area menggunakan Context (non-composable).
 */
fun getAreaDisplayName(area: String, context: android.content.Context): String =
    AreaFlags.getDisplayName(area, context)

/**
 * Menghasilkan nama tampilan area yang terlokalisasi sesuai bahasa aktif di aplikasi.
 * Jika key string tidak tersedia, secara aman mengembalikan nama asli dari API.
 */
@Composable
fun areaDisplayName(area: String): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return AreaFlags.getDisplayName(area, context)
}
