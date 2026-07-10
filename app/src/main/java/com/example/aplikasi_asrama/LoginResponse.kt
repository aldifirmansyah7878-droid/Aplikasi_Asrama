package com.example.aplikasi_asrama.api.model

data class LoginResponse(
    val status: String,
    val message: String,
    val data: UserData? // ← pastikan nama ini

)
