package com.karthik.aegis.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    val currentUserUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
}
