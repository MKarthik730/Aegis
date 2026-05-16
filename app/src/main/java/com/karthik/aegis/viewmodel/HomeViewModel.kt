package com.karthik.aegis.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.repository.FamilyRepository
import com.karthik.aegis.repository.LocationRepository
import com.karthik.aegis.repository.SOSRepository
import com.karthik.aegis.service.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val familyRepository: FamilyRepository,
    private val sosRepository: SOSRepository,
    @ApplicationContext private val context: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val familyMembers: StateFlow<List<FamilyMember>> = familyRepository
        .observeFamilyMembers("global")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyLocations: StateFlow<Map<String, TrackedLocation>> = locationRepository
        .observeFamilyLocations("global")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val activeAlerts: StateFlow<List<SOSAlert>> = sosRepository
        .observeSOSAlerts("global")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                val members = familyRepository.observeFamilyMembers("global").first()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun startLocationTracking() {
        LocationTrackingService.startTracking(context)
    }

    fun stopLocationTracking() {
        LocationTrackingService.stopTracking(context)
    }
}

data class HomeUiState(
    val currentUserName: String = "",
    val isTrackingEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)