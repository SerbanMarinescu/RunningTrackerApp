package com.example.core.data.di

import com.example.core.data.auth.EncryptedSessionStorage
import com.example.core.data.networking.HttpClientFactory
import com.example.core.domain.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }
    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()
}