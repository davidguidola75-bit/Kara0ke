package com.example

import com.example.data.model.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun parseLrcContent_parsesCorrectTimestampsAndLines() {
        val lrc = """
            [00:05.50]Bésame mucho
            [00:13.20]Como si fuera esta noche la última vez
            [01:05.00]Quiero tenerte muy cerca
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals(3, parsed.size)
        assertEquals(5500L, parsed[0].timestampMs)
        assertEquals("Bésame mucho", parsed[0].text)

        assertEquals(13200L, parsed[1].timestampMs)
        assertEquals("Como si fuera esta noche la última vez", parsed[1].text)

        assertEquals(65000L, parsed[2].timestampMs)
        assertEquals("Quiero tenerte muy cerca", parsed[2].text)
    }

    @Test
    fun parseLrcContent_handlesEmptyInputGracefully() {
        val parsed = LrcParser.parse("")
        assertTrue(parsed.isEmpty())
    }
}
