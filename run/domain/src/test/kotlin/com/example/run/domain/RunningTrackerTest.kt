@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.run.domain

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import com.example.core.connectivity.domain.messaging.MessagingAction
import com.example.core.domain.location.Location
import com.example.core.domain.location.LocationWithAltitude
import com.example.test.LocationObserverFake
import com.example.test.MainCoroutineExtension
import com.example.test.PhoneToWatchConnectorFake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.math.roundToInt

//@ExtendWith(MainCoroutineExtension::class)
class RunningTrackerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension = MainCoroutineExtension()
    }

    private lateinit var runningTracker: RunningTracker
    private lateinit var locationObserverFake: LocationObserverFake
    private lateinit var watchConnectorFake: PhoneToWatchConnectorFake

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    @BeforeEach
    fun setUp() {
        locationObserverFake = LocationObserverFake()
        watchConnectorFake = PhoneToWatchConnectorFake()
        testDispatcher = mainCoroutineExtension.testDispatcher
        testScope = CoroutineScope(testDispatcher)

        runningTracker = RunningTracker(
            locationObserver = locationObserverFake,
            applicationScope = testScope,
            watchConnector = watchConnectorFake
        )
    }

    @Test
    fun testCombiningRunData() {
        TestScope().launch {
            runningTracker.runData.test {
                skipItems(1)

                runningTracker.startObservingLocation()
                runningTracker.setIsTrackingRun(true)

                val location1 = LocationWithAltitude(Location(1.0, 1.0), 1.0)
                locationObserverFake.trackLocation(location1)
                val emission1 = awaitItem()

                val location2 = LocationWithAltitude(Location(2.0, 2.0), 2.0)
                locationObserverFake.trackLocation(location2)
                val emission2 = awaitItem()

                watchConnectorFake.sendMessageFromWatchToPhone(MessagingAction.HeartRateUpdate(160))
                val emission3 = awaitItem()

                watchConnectorFake.sendMessageFromWatchToPhone(MessagingAction.HeartRateUpdate(170))
                val emission4 = awaitItem()

                assertThat(emission1.locations[0][0].location).isEqualTo(location1)
                assertThat(emission2.locations[0][1].location).isEqualTo(location2)
                assertThat(emission3.heartRates).isEqualTo(160)
                assertThat(emission4.heartRates).isEqualTo(listOf(160, 170))

                val expectedDistance = 156.9 * 1000L
                val tolerance = 0.03
                val lowerBound = (expectedDistance * (1 - tolerance)).roundToInt()
                val upperBound = (expectedDistance * (1 + tolerance)).roundToInt()

                assertThat(emission4.distanceMeters).isBetween(lowerBound, upperBound)
                assertThat(emission4.locations.first()).hasSize(2)
            }
        }
    }
}