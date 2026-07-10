package com.example.aplikasi_asrama.api.model
import com.example.aplikasi_asrama.KamarData

data class KamarResponse(
    val status: String,
    val data: List<KamarData>?
)

