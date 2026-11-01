package com.example.sentinel_ip

import com.example.sentinel_ip.data.model.CountryCodeMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryCodeMapperTest {

    @Test
    fun testCountryNameAndAliasToIsoMapping() {
        // India mappings
        assertEquals("IN", CountryCodeMapper.mapToIsoCodes("INDIA"))
        assertEquals("IN", CountryCodeMapper.mapToIsoCodes("IND"))
        assertEquals("IN", CountryCodeMapper.mapToIsoCodes("in"))

        // United States mappings
        assertEquals("US", CountryCodeMapper.mapToIsoCodes("USA"))
        assertEquals("US", CountryCodeMapper.mapToIsoCodes("UNITED STATES"))
        assertEquals("US", CountryCodeMapper.mapToIsoCodes("US"))

        // United Kingdom mappings
        assertEquals("GB", CountryCodeMapper.mapToIsoCodes("UK"))
        assertEquals("GB", CountryCodeMapper.mapToIsoCodes("UNITED KINGDOM"))
        assertEquals("GB", CountryCodeMapper.mapToIsoCodes("GREAT BRITAIN"))

        // Germany, Japan, Russia
        assertEquals("DE", CountryCodeMapper.mapToIsoCodes("GERMANY"))
        assertEquals("JP", CountryCodeMapper.mapToIsoCodes("JAPAN"))
        assertEquals("RU", CountryCodeMapper.mapToIsoCodes("RUSSIA"))
    }

    @Test
    fun testMultiCountryCommaSeparatedMapping() {
        val result = CountryCodeMapper.mapToIsoCodes("INDIA, USA, GERMANY, UK")
        assertEquals("IN,US,DE,GB", result)
    }

    @Test
    fun testNullOrBlankHandling() {
        assertNull(CountryCodeMapper.mapToIsoCodes(null))
        assertNull(CountryCodeMapper.mapToIsoCodes("   "))
    }

    @Test
    fun testGetCountryName() {
        assertEquals("India", CountryCodeMapper.getCountryName("IN"))
        assertEquals("United States", CountryCodeMapper.getCountryName("US"))
        assertEquals("Germany", CountryCodeMapper.getCountryName("DE"))
        assertEquals("Global", CountryCodeMapper.getCountryName(null))
    }
}
