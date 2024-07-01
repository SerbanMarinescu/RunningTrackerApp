package com.example.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}