package com.example.sentinel_ip.data.model

import java.util.Locale

object CountryCodeMapper {

    private val countryMap = mapOf(
        "INDIA" to "IN", "IND" to "IN", "IN" to "IN",
        "UNITED STATES" to "US", "UNITED STATES OF AMERICA" to "US", "USA" to "US", "US" to "US",
        "UNITED KINGDOM" to "GB", "GREAT BRITAIN" to "GB", "UK" to "GB", "GB" to "GB",
        "GERMANY" to "DE", "DEU" to "DE", "DE" to "DE",
        "CANADA" to "CA", "CAN" to "CA", "CA" to "CA",
        "CHINA" to "CN", "CHN" to "CN", "CN" to "CN",
        "RUSSIA" to "RU", "RUSSIAN FEDERATION" to "RU", "RUS" to "RU", "RU" to "RU",
        "JAPAN" to "JP", "JPN" to "JP", "JP" to "JP",
        "FRANCE" to "FR", "FRA" to "FR", "FR" to "FR",
        "BRAZIL" to "BR", "BRA" to "BR", "BR" to "BR",
        "AUSTRALIA" to "AU", "AUS" to "AU", "AU" to "AU",
        "SOUTH KOREA" to "KR", "KOREA" to "KR", "KOR" to "KR", "KR" to "KR",
        "MEXICO" to "MX", "MEX" to "MX", "MX" to "MX",
        "NETHERLANDS" to "NL", "NLD" to "NL", "NL" to "NL",
        "SINGAPORE" to "SG", "SGP" to "SG", "SG" to "SG",
        "ITALY" to "IT", "ITA" to "IT", "IT" to "IT",
        "SPAIN" to "ES", "ESP" to "ES", "ES" to "ES",
        "SWITZERLAND" to "CH", "CHE" to "CH", "CH" to "CH", "SWISS" to "CH",
        "POLAND" to "PL", "POL" to "PL", "PL" to "PL",
        "UKRAINE" to "UA", "UKR" to "UA", "UA" to "UA",
        "VIETNAM" to "VN", "VNM" to "VN", "VN" to "VN",
        "TURKEY" to "TR", "TUR" to "TR", "TR" to "TR",
        "INDONESIA" to "ID", "IDN" to "ID", "ID" to "ID",
        "PAKISTAN" to "PK", "PAK" to "PK", "PK" to "PK",
        "BANGLADESH" to "BD", "BGD" to "BD", "BD" to "BD"
    )

    fun mapToIsoCodes(input: String?): String? {
        if (input.isNullOrBlank()) return null

        val tokens = input.split(",")
        val mappedCodes = tokens.mapNotNull { token ->
            val clean = token.trim().uppercase(Locale.ROOT)
            if (clean.isBlank()) null else countryMap[clean] ?: if (clean.length == 2) clean else null
        }.distinct()

        return if (mappedCodes.isNotEmpty()) mappedCodes.joinToString(",") else input.trim().uppercase(Locale.ROOT)
    }

    fun getCountryFlagEmoji(isoCode: String?): String {
        if (isoCode.isNullOrBlank() || isoCode.length != 2) return "🌐"
        val codeUpper = isoCode.trim().uppercase(Locale.ROOT)
        return try {
            val firstChar = Character.codePointAt(codeUpper, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(codeUpper, 1) - 0x41 + 0x1F1E6
            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        } catch (_: Exception) {
            "🌐"
        }
    }

    fun getCountryName(isoCode: String?): String {
        if (isoCode.isNullOrBlank()) return "Global"
        val codeUpper = isoCode.trim().uppercase(Locale.ROOT)
        return when (codeUpper) {
            "IN" -> "India"
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "DE" -> "Germany"
            "CA" -> "Canada"
            "CN" -> "China"
            "RU" -> "Russia"
            "JP" -> "Japan"
            "FR" -> "France"
            "BR" -> "Brazil"
            "AU" -> "Australia"
            "KR" -> "South Korea"
            "MX" -> "Mexico"
            "NL" -> "Netherlands"
            "SG" -> "Singapore"
            else -> try {
                Locale.Builder().setRegion(codeUpper).build().displayCountry.ifBlank { codeUpper }
            } catch (_: Exception) {
                codeUpper
            }
        }
    }
}
