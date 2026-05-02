package org.example.project.core.domain

interface RootError
sealed interface NetworkError : RootError {
    data object NoInternet : NetworkError
    data object RequestTimeout : NetworkError
    data object ServerError : NetworkError
    data object Serialization : NetworkError
    data object Unauthorized : NetworkError
    data object Unknown : NetworkError
}