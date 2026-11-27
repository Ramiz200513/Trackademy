package org.example.project.core.network

import org.example.project.core.domain.NetworkError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun mapTransformsSuccessData() {
        val result: Result<Int, NetworkError> = Result.Success(2)
        assertEquals(Result.Success("4"), result.map { (it * 2).toString() })
    }

    @Test
    fun mapKeepsErrorUntouched() {
        val result: Result<Int, NetworkError> = Result.Error(NetworkError.ServerError)
        assertEquals(Result.Error(NetworkError.ServerError), result.map { it * 2 })
    }

    @Test
    fun onSuccessAndOnErrorAreCalledOnlyForMatchingBranch() {
        var successCalled = false
        var errorCalled = false

        Result.Success(1).onSuccess { successCalled = true }.onError { errorCalled = true }
        assertTrue(successCalled)
        assertTrue(!errorCalled)

        successCalled = false
        Result.Error(NetworkError.Unknown).onSuccess { successCalled = true }.onError { errorCalled = true }
        assertTrue(!successCalled)
        assertTrue(errorCalled)
    }

    @Test
    fun getOrNullReturnsDataOrNull() {
        assertEquals(5, Result.Success(5).getOrNull())
        assertNull(Result.Error(NetworkError.Unknown).getOrNull())
    }
}
