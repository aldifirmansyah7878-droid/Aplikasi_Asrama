package com.example.aplikasi_asrama.api.model

data class PembayaranAdminRequest(
    val penghuni_id: Int,
    val bulanTahun: String,
    val jumlah: Int,
    val metodePembayaran: String
)