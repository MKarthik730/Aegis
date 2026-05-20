package com.karthik.aegis.ui.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.repository.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZoneViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository
) : ViewModel() {

    val safeZones: StateFlow<List<SafeZone>> = zoneRepository
        .observeSafeZones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ZoneUiState())
    val uiState: StateFlow<ZoneUiState> = _uiState.asStateFlow()

    fun addZone(zone: SafeZone) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                zoneRepository.addSafeZone(zone)
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Zone added")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun removeZone(zoneId: String) {
        viewModelScope.launch {
            try {
                zoneRepository.removeSafeZone(zoneId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateZone(zone: SafeZone) {
        viewModelScope.launch {
            try {
                zoneRepository.updateSafeZone(zone)
                _uiState.value = _uiState.value.copy(message = "Zone updated")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class ZoneUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
