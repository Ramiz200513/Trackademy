package org.example.project.core.network

import org.example.project.core.domain.RootError


sealed class Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>()
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>()
}

inline fun <D, E: RootError, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> {
    return when(this) {
        is Result.Success -> Result.Success(transform(this.data))
        is Result.Error -> Result.Error(this.error)
    }
}