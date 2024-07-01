package com.example.core.domain

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)
