package com.example.analytics.presentation

sealed interface AnalyticsAction {
    data object OnBackClick: AnalyticsAction
}