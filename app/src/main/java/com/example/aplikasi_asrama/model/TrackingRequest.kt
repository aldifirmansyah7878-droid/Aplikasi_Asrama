package com.example.aplikasi_asrama.api.model

data class TrackingRequest(
    val izin_id: Int,
    val user_id: Int,
    val latitude: Double,
    val longitude: Double,
    val alamat: String = ""
)