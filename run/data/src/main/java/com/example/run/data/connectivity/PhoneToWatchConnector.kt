package com.example.run.data.connectivity

import com.example.core.connectivity.domain.DeviceNode
import com.example.core.connectivity.domain.DeviceType
import com.example.core.connectivity.domain.NodeDiscovery
import com.example.run.domain.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope
): WatchConnector {

    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)

    private val isTrackable = MutableStateFlow(false)

    override val connectedDevice = _connectedNode.asStateFlow()

    val messagingActions = nodeDiscovery
        .observeConnectedDevices(DeviceType.PHONE)
        .onEach { connectedDevices ->
            val node = connectedDevices.firstOrNull()
            if(node != null && node.isNearby) {
                _connectedNode.value = node
            }
        }
        .launchIn(applicationScope)

    override fun setIsTrackable(isTrackable: Boolean) {
        this.isTrackable.value = isTrackable
    }
}