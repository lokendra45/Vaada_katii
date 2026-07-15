package com.gaatho.rent.core.network.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosConnectivityObserver : ConnectivityObserver {
    // Basic implementation for iOS using simple StateFlow for now
    // In a real app, this would use NWPathMonitor from Network framework
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = _isConnected.asStateFlow()
}
