package com.example.wear.run.data.di

import com.example.wear.run.data.HealthServicesExerciseTracker
import com.example.wear.run.data.connectivity.WatchToPhoneConnector
import com.example.wear.run.domain.ExerciseTracker
import com.example.wear.run.domain.PhoneConnector
import com.example.wear.run.domain.RunningTracker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val wearRunDataModule = module {
    singleOf(::HealthServicesExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<PhoneConnector>()
    singleOf(::RunningTracker)
}