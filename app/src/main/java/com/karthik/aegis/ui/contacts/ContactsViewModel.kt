package com.karthik.aegis.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.EmergencyContact
import com.karthik.aegis.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val contacts = contactsRepository.getEmergencyContacts()
                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load contacts"
                )
            }
        }
    }

    fun addContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                contactsRepository.addEmergencyContact(contact)
                loadContacts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add contact"
                )
            }
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            try {
                contactsRepository.removeEmergencyContact(contactId)
                loadContacts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete contact"
                )
            }
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                contactsRepository.updateEmergencyContact(contact)
                loadContacts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update contact"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
