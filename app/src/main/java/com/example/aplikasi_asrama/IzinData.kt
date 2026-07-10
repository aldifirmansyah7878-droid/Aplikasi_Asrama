package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class IzinData(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("namaPenghuni") val namaPenghuni: String,
    @SerializedName("nomorKamar") val nomorKamar: String,
    @SerializedName("keperluan") val keperluan: String,
    @SerializedName("tanggalKeluar") val tanggalKeluar: String,
    @SerializedName("jamKeluar") val jamKeluar: String,
    @SerializedName("perkiraanKembali") val perkiraanKembali: String,
    @SerializedName("waktuKembali") val waktuKembali: String? = null,
    @SerializedName("STATUS") val status: String, // "keluar" atau "kembali"
    @SerializedName("letakKunci") val letakKunci: String,
    @SerializedName("catatanKunci") val catatanKunci: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("alamatLokasi") val alamatLokasi: String,
    @SerializedName("createdAt") val createdAt: String
)

// Response wrapper untuk list
data class IzinListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<IzinData>?
)

// Response wrapper untuk single object
data class IzinResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: IzinData?
)

// Request untuk membuat izin keluar
data class IzinKeluarRequest(
    val userId: Int,
    val namaPenghuni: String,
    val nomorKamar: String,
    val keperluan: String,
    val tanggalKeluar: String,
    val jamKeluar: String,
    val perkiraanKembali: String,
    val letakKunci: String,
    val catatanKunci: String? = null,
    val latitude: Double,
    val longitude: Double,
    val alamatLokasi: String
)

// Request untuk update (hanya field tertentu)
data class UpdateIzinRequest(
    val perkiraanKembali: String? = null,
    val catatanKunci: String? = null,
    val letakKunci: String? = null
)