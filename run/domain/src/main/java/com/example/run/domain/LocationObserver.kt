package com.example.run.domain

import com.example.core.domain.location.LocationWithAltitude
import kotlinx.coroutines.flow.Flow

interface LocationObserver {
    fun observeLocation(interval: Long): Flow<LocationWithAltitude>
}