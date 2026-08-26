package com.lamanu.myplaces.ui.addplace

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamanu.myplaces.data.media.PhotoStorage
import com.lamanu.myplaces.data.repository.PlaceRepository
import com.lamanu.myplaces.domain.model.Mood
import com.lamanu.myplaces.domain.model.Moods
import com.lamanu.myplaces.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPlaceUiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val emoji: String = Moods.DEFAULT.emoji,
    val photoFileName: String? = null,
    val isSaving: Boolean = false,
    val savedPlaceId: String? = null,
    val errorMessage: String? = null,
) {
    val canSave: Boolean get() = title.isNotBlank() && !isSaving
}

@HiltViewModel
class AddPlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placeRepository: PlaceRepository,
    private val photoStorage: PhotoStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddPlaceUiState(
            latitude = savedStateHandle.get<String>(Destinations.ARG_LATITUDE)?.toDoubleOrNull() ?: 0.0,
            longitude = savedStateHandle.get<String>(Destinations.ARG_LONGITUDE)?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<AddPlaceUiState> = _uiState.asStateFlow()

    /** Fichier reserve pour une capture en cours : supprime si l'utilisateur annule. */
    private var pendingCapture: PhotoStorage.CaptureTarget? = null

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onMoodSelected(mood: Mood) = _uiState.update { it.copy(emoji = mood.emoji) }

    /** Prepare l'Uri a passer au contrat `TakePicture`. */
    fun prepareCapture(): Uri {
        discardPendingCapture()
        val target = photoStorage.newCaptureTarget()
        pendingCapture = target
        return target.uri
    }

    fun onCaptureResult(success: Boolean) {
        val target = pendingCapture ?: return
        pendingCapture = null
        if (!success) {
            viewModelScope.launch { photoStorage.delete(target.fileName) }
            return
        }
        viewModelScope.launch {
            photoStorage.compressInPlace(target.fileName)
            replacePhoto(target.fileName)
        }
    }

    fun onPhotoPickedFromGallery(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val fileName = photoStorage.importFromUri(uri)
            if (fileName == null) {
                _uiState.update { it.copy(errorMessage = "Impossible de lire cette image.") }
            } else {
                replacePhoto(fileName)
            }
        }
    }

    fun onRemovePhoto() {
        val current = _uiState.value.photoFileName ?: return
        _uiState.update { it.copy(photoFileName = null) }
        viewModelScope.launch { photoStorage.delete(current) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                placeRepository.createPlace(
                    title = state.title,
                    description = state.description,
                    emoji = state.emoji,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    photoFileName = state.photoFileName,
                )
            }.onSuccess { place ->
                _uiState.update { it.copy(isSaving = false, savedPlaceId = place.id) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = error.message ?: "Enregistrement impossible.")
                }
            }
        }
    }

    /** Annulation explicite : la photo deja copiee ne doit pas rester sur le disque. */
    fun discardDraft() {
        val orphan = _uiState.value.photoFileName
        discardPendingCapture()
        viewModelScope.launch { photoStorage.delete(orphan) }
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }

    private suspend fun replacePhoto(fileName: String) {
        val previous = _uiState.value.photoFileName
        _uiState.update { it.copy(photoFileName = fileName) }
        if (previous != null) photoStorage.delete(previous)
    }

    private fun discardPendingCapture() {
        val target = pendingCapture ?: return
        pendingCapture = null
        viewModelScope.launch { photoStorage.delete(target.fileName) }
    }
}
