package com.karthik.aegis.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.repository.FamilyRepository
import com.karthik.aegis.repository.LocationRepository
import com.karthik.aegis.repository.ZoneRepository
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val familyRepository: FamilyRepository,
    private val zoneRepository: ZoneRepository,
    private val prefs: AegisPrefs
) : ViewModel() {

    private val groupId = prefs.getFamilyGroupId() ?: "global"

    val familyMembers: StateFlow<List<FamilyMember>> = familyRepository
        .observeFamilyMembers(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyLocations: StateFlow<Map<String, TrackedLocation>> = locationRepository
        .observeFamilyLocations(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val safeZones: StateFlow<List<SafeZone>> = zoneRepository
        .observeSafeZones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()

    private val _selectedMemberLocation = MutableStateFlow<TrackedLocation?>(null)
    val selectedMemberLocation: StateFlow<TrackedLocation?> = _selectedMemberLocation.asStateFlow()

    fun selectMember(member: FamilyMember) {
        _selectedMember.value = member
        _selectedMemberLocation.value = familyLocations.value[member.uid]
    }

    fun clearSelection() {
        _selectedMember.value = null
        _selectedMemberLocation.value = null
    }
}
