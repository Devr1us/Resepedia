package com.example.resepedia

import com.example.resepedia.ui.util.AreaFlags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AreaFlagsTest {

    private val allApiAreas = listOf(
        "Afghan", "Albanian", "Algerian", "American", "Andorran", "Angolan", "Antiguan, Barbudan",
        "Argentine", "Argentinian", "Armenian", "Aruban", "Australian", "Austrian", "Azerbaijani",
        "Bahamian", "Bahraini", "Bangladeshi", "Barbadian", "Belarusian", "Belgian", "Belizean",
        "Beninese", "Bermudian", "Bhutanese", "Bolivian", "Bosnian, Herzegovinian", "Motswana",
        "Brazilian", "British", "Bruneian", "Bulgarian", "Burkinabe", "Burundian", "Cambodian",
        "Cameroonian", "Canadian", "Cape Verdian", "Caymanian", "Central African", "Chadian",
        "Chilean", "Chinese", "Colombian", "Congolese", "Costa Rican", "Croatian", "Cuban",
        "Cypriot", "Czech", "Danish", "Djibouti", "Dominican", "Dutch", "Ecuadorean", "Egyptian",
        "Emirati", "Equatorial Guinean", "Eritrean", "Estonian", "Ethiopian", "Faroese", "Fijian",
        "Filipino", "Finnish", "French", "Gabonese", "Gambian", "Georgian", "German", "Ghanaian",
        "Gibraltar", "Greek", "Greenlandic", "Grenadian", "Guadeloupian", "Guamanian", "Guatemalan",
        "Channel Islander", "Guinean", "Guinea-Bissauan", "Guyanese", "Haitian", "Honduran",
        "Hong Konger", "Hungarian", "Icelander", "Indian", "Indonesian", "Iranian", "Iraqi",
        "Irish", "Israeli", "Italian", "Ivorian", "Jamaican", "Japanese", "Jordanian", "Kazakhstani",
        "Kenyan", "Kirghiz", "Kosovar", "Kuwaiti", "Laotian", "Latvian", "Lebanese", "Mosotho",
        "Liberian", "Libyan", "Liechtensteiner", "Lithuanian", "Luxembourger", "Macedonian",
        "Malagasy", "Malawian", "Malaysian", "Maldivan", "Malian", "Maltese", "Mauritian",
        "Mexican", "Moldovan", "Mongolian", "Montenegrin", "Moroccan", "Mozambican", "Burmese",
        "Namibian", "Nepalese", "New Zealander", "Nicaraguan", "Nigerien", "Nigerian", "North Korean",
        "Norwegian", "Omani", "Pakistani", "Palestinian", "Panamanian", "Papua New Guinean",
        "Paraguayan", "Peruvian", "Polish", "Portuguese", "Puerto Rican", "Qatari", "Romanian",
        "Russian", "Rwandan", "Saint Lucian", "Salvadoran", "Sammarinese", "Samoan", "Saudi Arabian",
        "Senegalese", "Serbian", "Seychellois", "Sierra Leonean", "Singaporean", "Slovak", "Slovene",
        "Solomon Islander", "Somali", "South African", "South Korean", "South Sudanese", "Spanish",
        "Sri Lankan", "Sudanese", "Surinamer", "Swedish", "Swiss", "Syrian", "Tadzhik", "Taiwanese",
        "Tanzanian", "Thai", "Togolese", "Tongan", "Trinidadian", "Tunisian", "Turkish", "Turkmen",
        "Tuvaluan", "Ugandan", "Ukrainian", "Uruguayan", "Uzbekistani", "Ni-Vanuatu", "Venezuelan",
        "Vietnamese", "Yemeni", "Zambian", "Zimbabwean"
    )

    @Test
    fun testAllApiAreasHaveNonFallbackFlags() {
        for (area in allApiAreas) {
            val iso = AreaFlags.getIsoCode(area)
            assertNotNull("Area '$area' harus memiliki kode ISO", iso)
            val flag = AreaFlags.getFlag(area)
            assertNotEquals("Area '$area' tidak boleh memakai fallback flag 🌍", AreaFlags.FALLBACK_FLAG, flag)
        }
    }

    @Test
    fun testUnknownAreaFallback() {
        assertEquals(AreaFlags.FALLBACK_FLAG, AreaFlags.getFlag("Unknown"))
        assertEquals(AreaFlags.FALLBACK_FLAG, AreaFlags.getFlag(""))
    }

    @Test
    fun testCommonVariations() {
        assertEquals("🇮🇩", AreaFlags.getFlag("Indonesia"))
        assertEquals("🇮🇩", AreaFlags.getFlag("Indonesian"))
        assertEquals("🇺🇸", AreaFlags.getFlag("American"))
        assertEquals("🇺🇸", AreaFlags.getFlag("United States"))
        assertEquals("🇬🇧", AreaFlags.getFlag("British"))
        assertEquals("🇬🇧", AreaFlags.getFlag("English"))
        assertEquals("🇬🇧", AreaFlags.getFlag("Scottish"))
        assertEquals("🇬🇧", AreaFlags.getFlag("Welsh"))
        assertEquals("🇸🇦", AreaFlags.getFlag("Saudi Arabian"))
        assertEquals("🇰🇷", AreaFlags.getFlag("South Korean"))
        assertEquals("🇰🇷", AreaFlags.getFlag("Korean"))
        assertEquals("🇸🇰", AreaFlags.getFlag("Slovak"))
        assertEquals("🇸🇰", AreaFlags.getFlag("Slovakian"))
        assertEquals("🇻🇪", AreaFlags.getFlag("Venezuelan"))
        assertEquals("🇻🇪", AreaFlags.getFlag("Venezulan")) // TheMealDB typo
    }
}
