package org.example.project.data.repository

import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.core.network.createHttpClient
import org.example.project.core.session.SessionStorage
import org.example.project.core.session.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthRepositoryTest {

    private val session = SessionStorage(MapSettings())
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun repository(engine: MockEngine) = AuthRepositoryImpl(
        httpClient = createHttpClient(
            session = session,
            baseUrl = "https://api.test/",
            enableLogging = false,
            engine = engine
        ),
        session = session
    )

    @Test
    fun successfulLoginStoresTokensOrganizationAndRole() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/Auth/login", request.url.encodedPath)
            respond(
                content = ByteReadChannel(
                    """{"token":"access","refreshToken":"refresh",
                       "user":{"fullName":"Иван Иванов","role":"Teacher"}}"""
                ),
                status = HttpStatusCode.OK,
                headers = jsonHeaders
            )
        }

        val result = repository(engine).login("ivan", "secret", "org-1")

        assertEquals(Result.Success(Unit), result)
        assertTrue(session.isLoggedIn)
        assertEquals("access", session.accessToken)
        assertEquals("refresh", session.refreshToken)
        assertEquals("org-1", session.organizationId)
        assertEquals("Иван Иванов", session.fullName)
        assertEquals(UserRole.Teacher, session.userRole)
    }

    @Test
    fun wrongCredentialsReturnUnauthorizedAndKeepUserLoggedOut() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.Unauthorized) }

        val result = repository(engine).login("ivan", "wrong", "org-1")

        assertEquals(Result.Error(NetworkError.Unauthorized), result)
        assertFalse(session.isLoggedIn)
    }

    @Test
    fun serverErrorIsMappedToServerError() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }

        val result = repository(engine).getOrganizations()

        assertEquals(Result.Error(NetworkError.ServerError), result)
    }

    @Test
    fun malformedJsonIsMappedToSerializationError() = runTest {
        val engine = MockEngine {
            respond(ByteReadChannel("""{"not":"a list"}"""), HttpStatusCode.OK, jsonHeaders)
        }

        val result = repository(engine).getOrganizations()

        assertEquals(Result.Error(NetworkError.Serialization), result)
    }
}
