package com.example.aplikasi_asrama.model

data class Pembayaran(
    val id: Int,
    val namaPenghuni: String,
    val nomorKamar: String,
    val bulanTahun: String,
    val jumlah: Double,
    val tanggalBayar: String,
    val status: String
)