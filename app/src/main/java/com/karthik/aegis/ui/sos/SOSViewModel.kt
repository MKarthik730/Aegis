package com.karthik.aegis.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.repository.ContactsRepository
import com.karthik.aegis.repository.SOSRepository
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val sosRepository: SOSRepository,
    private val contactsRepository: ContactsRepository,
    private val prefs: AegisPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSUiState())
    val uiState: StateFlow<SOSUiState> = _uiState.asStateFlow()

    private val groupId = prefs.getFamilyGroupId() ?: "global"

    val activeAlerts: StateFlow<List<SOSAlert>> = sosRepository
        .observeSOSAlerts(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerSOS(reason: String, latitude: Double = 0.0, longitude: Double = 0.0) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val contacts = contactsRepository.getEmergencyContacts()
                
                sosRepository.triggerSOSWithReason(
                    contacts = contacts,
                    reason = reason,
                    isAutomatic = false,
                    latitude = latitude,
                    longitude = longitude,
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "SOS sent successfully!"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun resolveSOSAlert(uid: String) {
        viewModelScope.launch {
            try {
                sosRepository.resolveSOSAlert(uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun sendSafeNotification() {
        viewModelScope.launch {
            try {
                val contacts = contactsRepository.getEmergencyContacts()
                sosRepository.sendSafeNotification(contacts)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class SOSUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
