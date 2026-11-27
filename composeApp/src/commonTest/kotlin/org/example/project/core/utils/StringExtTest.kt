package org.example.project.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtTest {

    @Test
    fun initialsAreTakenFromFirstTwoWords() {
        assertEquals("ИИ", "Иванов Иван Петрович".toInitials())
    }

    @Test
    fun extraSpacesAreIgnoredAndLettersAreUppercased() {
        assertEquals("AB", "  anna   bell ".toInitials())
    }

    @Test
    fun blankInputReturnsFallback() {
        assertEquals("??", "   ".toInitials())
    }
}
