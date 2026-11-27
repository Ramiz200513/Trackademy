package org.example.project.core.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.logging.AppLogger
import kotlin.coroutines.CoroutineContext

/**
 * Base class for screen components (MVI style).
 *
 * - [state] holds the immutable UI state, changed only through [updateState];
 * - [effects] delivers one-off events (navigation, toasts) exactly once;
 * - [scope] is bound to the component lifecycle and is cancelled on destroy.
 *   It uses a SupervisorJob, so one failed child does not cancel the others,
 *   and an exception handler that logs instead of crashing the app.
 *
 * @param S UI state type
 * @param E one-off effect type (use [Nothing] if the screen has none)
 */
abstract class BaseComponent<S : Any, E : Any>(
    componentContext: ComponentContext,
    initialState: S,
    mainContext: CoroutineContext = Dispatchers.Main.immediate
) : ComponentContext by componentContext {

    protected open val tag: String = this::class.simpleName ?: "Component"

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.e(tag, "Unhandled coroutine exception", throwable)
    }

    protected val scope: CoroutineScope =
        coroutineScope(mainContext + SupervisorJob() + exceptionHandler)

    protected val currentState: S get() = _state.value

    protected fun updateState(reducer: S.() -> S) = _state.update(reducer)

    protected fun sendEffect(effect: E) {
        scope.launch { _effects.send(effect) }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        scope.launch(block = block)
}
