package org.example.project.core.session

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionStorageTest {

    private val session = SessionStorage(MapSettings())

    @Test
    fun newSessionIsLoggedOut() {
        assertFalse(session.isLoggedIn)
        assertNull(session.accessToken)
        assertEquals(UserRole.Unknown, session.userRole)
    }

    @Test
    fun saveTokensKeepsPreviousRefreshTokenWhenNewOneIsNull() {
        session.saveTokens("access-1", "refresh-1")
        session.saveTokens("access-2", null)

        assertTrue(session.isLoggedIn)
        assertEquals("access-2", session.accessToken)
        assertEquals("refresh-1", session.refreshToken)
    }

    @Test
    fun clearTokensLogsOutButKeepsOrganization() {
        session.organizationId = "org-1"
        session.saveTokens("access", "refresh")

        session.clearTokens()

        assertFalse(session.isLoggedIn)
        assertNull(session.refreshToken)
        assertEquals("org-1", session.organizationId)
    }

    @Test
    fun userRoleIsPersisted() {
        session.userRole = UserRole.Teacher
        assertEquals(UserRole.Teacher, session.userRole)
    }

    @Test
    fun apiRoleStringsAreParsed() {
        assertEquals(UserRole.Student, UserRole.fromApi("Student"))
        assertEquals(UserRole.Teacher, UserRole.fromApi("Teacher"))
        assertEquals(UserRole.Administrator, UserRole.fromApi("Administrator"))
        assertEquals(UserRole.Unknown, UserRole.fromApi("Guest"))
        assertEquals(UserRole.Unknown, UserRole.fromApi(null))
    }
}
