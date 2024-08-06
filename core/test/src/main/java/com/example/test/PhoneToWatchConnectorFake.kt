package com.example.test

import com.example.core.connectivity.domain.DeviceNode
import com.example.core.connectivity.domain.messaging.MessagingAction
import com.example.core.connectivity.domain.messaging.MessagingError
import com.example.core.domain.util.EmptyResult
import com.example.core.domain.util.Result
import com.example.run.domain.WatchConnector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class PhoneToWatchConnectorFake: WatchConnector {

    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)

    override val connectedDevice: StateFlow<DeviceNode?>
        get() = _connectedDevice.asStateFlow()

    private val _messagingActions = MutableSharedFlow<MessagingAction>()

    override val messagingActions: Flow<MessagingAction>
        get() = _messagingActions.asSharedFlow()

    var sendError: MessagingError? = null

    private val _isTrackable = MutableStateFlow(true)

    override suspend fun sendActionToWatch(action: MessagingAction): EmptyResult<MessagingError> {
        return if(sendError == null) {
            Result.Success(Unit)
        } else {
            Result.Error(sendError!!)
        }
    }

    override fun setIsTrackable(isTrackable: Boolean) {
        _isTrackable.value = isTrackable
    }

    suspend fun sendMessageFromWatchToPhone(action: MessagingAction) {
        _messagingActions.emit(action)
    }
}