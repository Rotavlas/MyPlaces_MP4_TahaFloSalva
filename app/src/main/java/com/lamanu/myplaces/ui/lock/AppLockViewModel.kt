package com.lamanu.myplaces.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.fragment.app.FragmentActivity
import com.lamanu.myplaces.core.biometric.BiometricAuthenticator
import com.lamanu.myplaces.data.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LockState {
    /** Etat initial, le temps de lire les preferences. */
    data object Checking : LockState
    data object Locked : LockState
    data class Failed(val message: String) : LockState
    data object Unlocked : LockState
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private val _state = MutableStateFlow<LockState>(LockState.Checking)
    val state: StateFlow<LockState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val locked = userPreferences.preferences.first().biometricLockEnabled
            _state.value = if (locked) LockState.Locked else LockState.Unlocked
        }
    }

    /** Declenche le BiometricPrompt ; l'activite est fournie par l'UI, jamais retenue ici. */
    fun requestUnlock(activity: FragmentActivity) {
        biometricAuthenticator.authenticate(
            activity = activity,
            onSuccess = ::onUnlocked,
            onFailure = ::onFailure,
            onCancelled = ::onCancelled,
        )
    }

    fun onUnlocked() {
        _state.value = LockState.Unlocked
    }

    fun onFailure(message: String) {
        _state.value = LockState.Failed(message)
    }

    fun onCancelled() {
        _state.value = LockState.Locked
    }
}
