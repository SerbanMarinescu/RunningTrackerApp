package com.example.auth.presentation.login

import com.example.core.presentation.ui.UiText

sealed interface LoginEvent {
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
}