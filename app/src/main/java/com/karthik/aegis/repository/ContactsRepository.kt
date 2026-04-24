package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karthik.aegis.model.EmergencyContact
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getEmergencyContacts(): List<EmergencyContact> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = database.child("emergency_contacts").child(uid).get().await()
            snapshot.children.mapNotNull { child ->
                EmergencyContact(
                    id = child.key ?: "",
                    name = child.child("name").getValue(String::class.java) ?: "",
                    phone = child.child("phone").getValue(String::class.java) ?: "",
                    fcmToken = child.child("fcmToken").getValue(String::class.java) ?: "",
                    relation = child.child("relation").getValue(String::class.java) ?: "",
                    isPrimary = child.child("isPrimary").getValue(Boolean::class.java) ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addEmergencyContact(contact: EmergencyContact) {
        val uid = auth.currentUser?.uid ?: return
        val contactId = contact.id.ifEmpty { database.child("emergency_contacts").child(uid).push().key ?: return }
        
        database.child("emergency_contacts").child(uid).child(contactId).setValue(contact.copy(id = contactId)).await()
    }

    suspend fun removeEmergencyContact(contactId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.child("emergency_contacts").child(uid).child(contactId).removeValue().await()
    }

    suspend fun updateEmergencyContact(contact: EmergencyContact) {
        val uid = auth.currentUser?.uid ?: return
        database.child("emergency_contacts").child(uid).child(contact.id).setValue(contact).await()
    }
}