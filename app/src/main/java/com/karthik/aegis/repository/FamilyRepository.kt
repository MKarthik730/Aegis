package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karthik.aegis.model.FamilyMember
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun observeFamilyMembers(groupId: String): Flow<List<FamilyMember>> = callbackFlow {
        val query = database.child("family_groups").child(groupId).child("members")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val members = snapshot.children.mapNotNull {
                    it.getValue(FamilyMember::class.java)
                }
                trySend(members)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addFamilyMember(groupId: String, member: FamilyMember) {
        val memberId = member.uid.ifEmpty { database.child("family_groups").child(groupId).child("members").push().key ?: return }
        database.child("family_groups").child(groupId).child("members").child(memberId)
            .setValue(member.copy(uid = memberId)).await()
    }

    suspend fun removeFamilyMember(groupId: String, uid: String) {
        database.child("family_groups").child(groupId).child("members").child(uid).removeValue().await()
    }

    suspend fun updateMemberStatus(groupId: String, uid: String, status: String) {
        database.child("family_groups").child(groupId).child("members").child(uid).child("status").setValue(status).await()
    }

    suspend fun createFamilyGroup(groupName: String): String {
        val currentUser = auth.currentUser ?: return ""
        val groupId = database.child("family_groups").push().key ?: return ""

        val member = FamilyMember(
            uid = currentUser.uid,
            name = currentUser.displayName ?: "User",
            email = currentUser.email ?: "",
            phone = "",
            role = "ADMIN"
        )

        val updates = hashMapOf<String, Any>(
            "family_groups/$groupId/name" to groupName,
            "family_groups/$groupId/members/${currentUser.uid}" to member
        )
        database.updateChildren(updates).await()

        return groupId
    }
}
