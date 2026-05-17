package com.karthik.aegis.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.EmergencyContact
import com.karthik.aegis.repository.ContactsRepository
import com.karthik.aegis.repository.SOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SOSPhase {
    data object Idle : SOSPhase()
    data object Confirming : SOSPhase()
    data class Countdown(val secondsLeft: Int) : SOSPhase()
    data object Active : SOSPhase()
    data object Resolved : SOSPhase()
}

data class SOSUiState(
    val phase: SOSPhase = SOSPhase.Idle,
    val contacts: List<EmergencyContact> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val sosRepository: SOSRepository,
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSUiState())
    val uiState: StateFlow<SOSUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val contacts = contactsRepository.getEmergencyContacts()
                _uiState.value = _uiState.value.copy(contacts = contacts, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load contacts"
                )
            }
        }
    }

    fun requestConfirmation() {
        _uiState.value = _uiState.value.copy(phase = SOSPhase.Confirming)
    }

    fun cancelConfirmation() {
        _uiState.value = _uiState.value.copy(phase = SOSPhase.Idle)
    }

    fun startCountdown(reason: String = "Manual SOS") {
        _uiState.value = _uiState.value.copy(phase = SOSPhase.Countdown(secondsLeft = 30))
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 30 downTo 1) {
                _uiState.value = _uiState.value.copy(phase = SOSPhase.Countdown(secondsLeft = sec))
                delay(1000L)
            }
            triggerSOS(reason)
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(phase = SOSPhase.Idle)
    }

    private fun triggerSOS(reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                sosRepository.triggerSOSWithReason(
                    contacts = _uiState.value.contacts,
                    reason = reason,
                    isAutomatic = false,
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            phase = SOSPhase.Active,
                            isLoading = false
                        )
                    },
                    onFailure = { errorMsg ->
                        _uiState.value = _uiState.value.copy(
                            phase = SOSPhase.Idle,
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = SOSPhase.Idle,
                    isLoading = false,
                    error = e.message ?: "Failed to trigger SOS"
                )
            }
        }
    }

    fun sendSafeSignal() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                sosRepository.sendSafeNotification(_uiState.value.contacts)
                _uiState.value = _uiState.value.copy(
                    phase = SOSPhase.Resolved,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send safe signal"
                )
            }
        }
    }

    fun resolveSOS() {
        viewModelScope.launch {
            try {
                sosRepository.resolveSOSAlert("")
                _uiState.value = _uiState.value.copy(phase = SOSPhase.Idle)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to resolve SOS"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
