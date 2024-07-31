@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.run.domain

import com.example.core.connectivity.domain.messaging.MessagingAction
import com.example.core.domain.Timer
import com.example.core.domain.location.LocationTimeStamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class RunningTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
    private val watchConnector: WatchConnector
) {

    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()

    private val _isTrackingRun = MutableStateFlow(false)
    val isTrackingRun = _isTrackingRun.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    val currentLocation = isObservingLocation
        .flatMapLatest { isObservingLocation ->
            if(isObservingLocation) {
                locationObserver.observeLocation(1000L)
            } else flowOf()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    private val heartRates = isTrackingRun
        .flatMapLatest { isTracking ->
            if(isTracking) {
                watchConnector.messagingActions
            } else flowOf()
        }
        .filterIsInstance<MessagingAction.HeartRateUpdate>()
        .map { it.heartRate }
        .runningFold(initial = emptyList<Int>()) { currentHeartRate, newHeartRate ->
            currentHeartRate + newHeartRate
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            emptyList()
        )

    init {
        _isTrackingRun
            .onEach { isTrackingRun ->
                if(!isTrackingRun) {
                    val newList = buildList {
                        addAll(runData.value.locations)
                        add(emptyList<LocationTimeStamp>())
                    }.toList()
                    _runData.update {
                        it.copy(
                            locations = newList
                        )
                    }
                }
            }
            .flatMapLatest { isTrackingRun ->
                if(isTrackingRun) {
                    Timer.timeAndEmit()
                } else flowOf()
            }
            .onEach { duration ->
                _elapsedTime.value += duration
            }
            .launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(_isTrackingRun) { location, isTracking ->
                if(isTracking) {
                    emit(location)
                }
            }
            .zip(_elapsedTime) { location, elapsedTime ->
                LocationTimeStamp(
                    location = location,
                    durationTimestamp = elapsedTime
                )
            }
            .combine(heartRates) { locationTimestamp, heartRates ->

                val currentLocationsList = runData.value.locations
                val lastLocationsList = if(currentLocationsList.isNotEmpty()) {
                    currentLocationsList.last() + locationTimestamp
                } else listOf(locationTimestamp)
                val newLocationsList = currentLocationsList.replaceLast(lastLocationsList)

                val distanceMeters = LocationDataCalculator.getTotalDistanceMeters(newLocationsList)
                val distanceKm = distanceMeters / 1000.0
                val currentDuration = locationTimestamp.durationTimestamp

                val avgSecondsPerKm = if(distanceKm == 0.0) {
                    0
                } else {
                    (currentDuration.inWholeSeconds / distanceKm).roundToInt()
                }

                _runData.update {
                    RunData(
                        distanceMeters = distanceMeters,
                        pace = avgSecondsPerKm.seconds,
                        locations = newLocationsList,
                        heartRates = heartRates
                    )
                }
            }
            .launchIn(applicationScope)

        elapsedTime
            .onEach {
                watchConnector.sendActionToWatch(MessagingAction.TimeUpdate(it))
            }
            .launchIn(applicationScope)

        runData
            .map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach {
                watchConnector.sendActionToWatch(MessagingAction.DistanceUpdate(it))
            }
            .launchIn(applicationScope)
    }

    fun setIsTrackingRun(isTracking: Boolean) {
        this._isTrackingRun.value = isTracking
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
        watchConnector.setIsTrackable(true)
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
        watchConnector.setIsTrackable(false)
    }

    private fun <T> List<List<T>>.replaceLast(replacement: List<T>): List<List<T>> {
        if(this.isEmpty()) {
            return listOf(replacement)
        }
        return this.dropLast(1) + listOf(replacement)
    }

    fun finishRun() {
        stopObservingLocation()
        setIsTrackingRun(false)
        _elapsedTime.value = ZERO
        _runData.value = RunData()
    }
}