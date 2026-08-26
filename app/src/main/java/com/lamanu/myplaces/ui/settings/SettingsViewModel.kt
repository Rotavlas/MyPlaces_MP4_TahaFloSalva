package com.lamanu.myplaces.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamanu.myplaces.core.biometric.BiometricAuthenticator
import com.lamanu.myplaces.core.biometric.BiometricAvailability
import com.lamanu.myplaces.data.prefs.UserPreferencesRepository
import com.lamanu.myplaces.data.repository.JournalTransferRepository
import com.lamanu.myplaces.data.repository.PlaceRepository
import com.lamanu.myplaces.data.transfer.ImportReport
import com.lamanu.myplaces.domain.model.Author
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val author: Author = Author(id = "", name = ""),
    val biometricLockEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.UNAVAILABLE,
    val ownPlaces: Int = 0,
    val importedPlaces: Int = 0,
    val isBusy: Boolean = false,
    val message: String? = null,
    val lastImport: ImportReport? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val transferRepository: JournalTransferRepository,
    private val placeRepository: PlaceRepository,
    biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(biometricAvailability = biometricAuthenticator.availability()),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.preferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        author = prefs.author,
                        biometricLockEnabled = prefs.biometricLockEnabled,
                    )
                }
            }
        }
        refreshCounts()
    }

    fun onAuthorNameChange(name: String) {
        _uiState.update { it.copy(author = it.author.copy(name = name)) }
        viewModelScope.launch { userPreferences.setAuthorName(name) }
    }

    fun onBiometricLockChange(enabled: Boolean) {
        if (enabled && !_uiState.value.biometricAvailability.canBeEnabled) {
            _uiState.update { it.copy(message = "Configurez d'abord une empreinte ou un code d'ecran.") }
            return
        }
        viewModelScope.launch { userPreferences.setBiometricLockEnabled(enabled) }
    }

    fun suggestedExportFileName(): String = transferRepository.suggestedFileName()

    fun export(destination: Uri) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            transferRepository.exportTo(destination)
                .onSuccess { count ->
                    _uiState.update { it.copy(isBusy = false, message = "$count lieu(x) exporte(s).") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isBusy = false, message = error.message ?: "Export impossible.")
                    }
                }
        }
    }

    fun import(source: Uri) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            transferRepository.importFrom(source)
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            lastImport = report,
                            message = buildImportMessage(report),
                        )
                    }
                    refreshCounts()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isBusy = false, message = error.message ?: "Import impossible.")
                    }
                }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    private fun refreshCounts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    ownPlaces = placeRepository.countOwnPlaces(),
                    importedPlaces = placeRepository.countImportedPlaces(),
                )
            }
        }
    }

    private fun buildImportMessage(report: ImportReport): String = buildString {
        append("${report.imported} lieu(x) de ${report.fileAuthor.name} importe(s)")
        if (report.duplicatesSkipped > 0) append(", ${report.duplicatesSkipped} deja present(s)")
        if (report.invalidSkipped > 0) append(", ${report.invalidSkipped} invalide(s)")
        if (report.ownPlacesSkipped > 0) append(", ${report.ownPlacesSkipped} deja a moi")
        append(".")
    }
}
