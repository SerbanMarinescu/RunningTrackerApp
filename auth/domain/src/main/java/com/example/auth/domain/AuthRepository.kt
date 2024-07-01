package com.example.auth.domain

import com.example.core.domain.util.DataError
import com.example.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun register(email: String, password: String): EmptyResult<DataError.Network>
}