package org.example.project.core.ui.error

import org.example.project.core.domain.NetworkError

/** User-facing message for a [NetworkError]. */
fun NetworkError.toUserMessage(): String = when (this) {
    NetworkError.NoInternet -> "Нет подключения к интернету"
    NetworkError.RequestTimeout -> "Сервер не отвечает. Попробуйте ещё раз"
    NetworkError.ServerError -> "Ошибка сервера. Попробуйте позже"
    NetworkError.Serialization -> "Не удалось обработать ответ сервера"
    NetworkError.Unauthorized -> "Сессия истекла. Войдите заново"
    NetworkError.Forbidden -> "Недостаточно прав"
    NetworkError.NotFound -> "Данные не найдены"
    NetworkError.BadRequest -> "Проверьте введённые данные"
    NetworkError.Unknown -> "Что-то пошло не так"
}
