package com.scrollstop.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Global singleton holding the live accessibility service state.
 * Avoids Context-based polling for the running state.
 */
object ServiceStateHolder {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun setServiceRunning(running: Boolean) {
        _isRunning.value = running
    }
}
