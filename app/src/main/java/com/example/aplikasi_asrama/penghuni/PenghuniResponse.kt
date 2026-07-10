package com.example.aplikasi_asrama.api.model

import com.example.aplikasi_asrama.PenghuniData

data class PenghuniResponse(
    val status: String,
    val data: List<PenghuniData>?
)

