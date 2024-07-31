package com.example.run.domain

import com.example.core.domain.location.LocationTimeStamp
import kotlin.time.Duration

data class RunData(
    val distanceMeters: Int = 0,
    val pace: Duration = Duration.ZERO,
    val locations: List<List<LocationTimeStamp>> = emptyList(),
    val heartRates: List<Int> = emptyList()
)
