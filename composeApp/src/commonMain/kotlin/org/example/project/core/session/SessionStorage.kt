package org.example.project.core.session

import com.russhwolf.settings.Settings

/**
 * Single source of truth for the persisted user session (tokens, organization, role).
 * Keys are kept compatible with previously stored data.
 */
class SessionStorage(private val settings: Settings) {

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_TOKEN)?.takeIf { it.isNotBlank() }
        set(value) = settings.putOrRemove(KEY_TOKEN, value)

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)?.takeIf { it.isNotBlank() }
        set(value) = settings.putOrRemove(KEY_REFRESH_TOKEN, value)

    var organizationId: String
        get() = settings.getString(KEY_ORG_ID, "")
        set(value) = settings.putString(KEY_ORG_ID, value)

    var fullName: String?
        get() = settings.getStringOrNull(KEY_FULL_NAME)
        set(value) = settings.putOrRemove(KEY_FULL_NAME, value)

    var userRole: UserRole
        get() = UserRole.fromCode(settings.getInt(KEY_USER_ROLE, UserRole.Unknown.code))
        set(value) = settings.putInt(KEY_USER_ROLE, value.code)

    val isLoggedIn: Boolean get() = accessToken != null

    fun saveTokens(accessToken: String, refreshToken: String?) {
        this.accessToken = accessToken
        if (refreshToken != null) this.refreshToken = refreshToken
    }

    fun clearTokens() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    fun clear() = settings.clear()

    private fun Settings.putOrRemove(key: String, value: String?) {
        if (value == null) remove(key) else putString(key, value)
    }

    companion object {
        const val KEY_TOKEN = "token"
        const val KEY_REFRESH_TOKEN = "refreshToken"
        const val KEY_ORG_ID = "orgId"
        const val KEY_FULL_NAME = "fullName"
        const val KEY_USER_ROLE = "userRole"
    }
}

enum class UserRole(val code: Int) {
    Unknown(0), Student(1), Administrator(2), Teacher(3);

    companion object {
        fun fromCode(code: Int): UserRole = entries.firstOrNull { it.code == code } ?: Unknown

        fun fromApi(role: String?): UserRole = when (role) {
            "Student" -> Student
            "Administrator" -> Administrator
            "Teacher" -> Teacher
            else -> Unknown
        }
    }
}
