package com.thecode.cryptomania.domain.model

enum class ThemePreference { System, Light, Dark }

data class UserSettings(
    val theme: ThemePreference = ThemePreference.System,
    /** Swap green/red for blue/orange so price direction does not rely on red–green vision. */
    val colorBlindFriendly: Boolean = false,
    val onboardingCompleted: Boolean = false,
)
