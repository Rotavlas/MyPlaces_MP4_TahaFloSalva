package com.lamanu.myplaces.data.prefs

import com.lamanu.myplaces.domain.model.Author

data class UserPreferences(
    val author: Author,
    val biometricLockEnabled: Boolean,
    val onboardingDone: Boolean,
)
