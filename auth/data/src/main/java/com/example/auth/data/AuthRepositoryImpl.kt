package com.example.auth.data

import com.example.auth.domain.AuthRepository
import com.example.core.data.networking.post
import com.example.core.domain.util.DataError
import com.example.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val httpClient: HttpClient
): AuthRepository {

    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
        return httpClient.post<RegisterRequest, Unit>(
            route = "/register",
            body = RegisterRequest(
                email = email,
                password = password
            )
        )
    }
}