package com.example.aplikasi_asrama.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class untuk lokasi pengguna
data class UserLocation(
    val userId: String,
    val lat: Double,
    val lng: Double,
    val timestamp: String,
    val accuracy: Float
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance().getReference("locations")
    private val auth = FirebaseAuth.getInstance()
    private val _otherUsersLocations = MutableStateFlow<List<UserLocation>>(emptyList())
    val otherUsersLocations: StateFlow<List<UserLocation>> = _otherUsersLocations.asStateFlow()

    private var locationListener: ChildEventListener? = null

    init {
        listenForOtherLocations()
    }

    private fun listenForOtherLocations() {
        val currentUserId = auth.currentUser?.uid ?: return
        locationListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key ?: return
                if (userId != currentUserId) {
                    val data = snapshot.getValue<Map<String, Any>>()
                    data?.let {
                        val loc = UserLocation(
                            userId = userId,
                            lat = it["lat"] as? Double ?: 0.0,
                            lng = it["lng"] as? Double ?: 0.0,
                            timestamp = it["timestamp"] as? String ?: "",
                            accuracy = (it["accuracy"] as? Double)?.toFloat() ?: 0f
                        )
                        val currentList = _otherUsersLocations.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { existing -> existing.userId == userId }
                        if (existingIndex != -1) {
                            currentList[existingIndex] = loc
                        } else {
                            currentList.add(loc)
                        }
                        _otherUsersLocations.value = currentList
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key ?: return
                if (userId != currentUserId) {
                    val data = snapshot.getValue<Map<String, Any>>()
                    data?.let {
                        val loc = UserLocation(
                            userId = userId,
                            lat = it["lat"] as? Double ?: 0.0,
                            lng = it["lng"] as? Double ?: 0.0,
                            timestamp = it["timestamp"] as? String ?: "",
                            accuracy = (it["accuracy"] as? Double)?.toFloat() ?: 0f
                        )
                        val currentList = _otherUsersLocations.value.toMutableList()
                        val index = currentList.indexOfFirst { existing -> existing.userId == userId }
                        if (index != -1) {
                            currentList[index] = loc
                            _otherUsersLocations.value = currentList
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val userId = snapshot.key ?: return
                _otherUsersLocations.value = _otherUsersLocations.value.filter { it.userId != userId }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }
        database.addChildEventListener(locationListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        locationListener?.let { database.removeEventListener(it) }
    }

    fun removeMyLocation() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).removeValue()
    }
}