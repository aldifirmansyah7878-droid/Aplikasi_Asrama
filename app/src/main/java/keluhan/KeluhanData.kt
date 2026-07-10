package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class KeluhanData(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("namaPenghuni") val namaPenghuni: String,
    @SerializedName("nomorKamar") val nomorKamar: String,
    @SerializedName("judulKeluhan") val judulKeluhan: String,
    @SerializedName("deskripsiKeluhan") val deskripsiKeluhan: String,
    @SerializedName("fotoKeluhanUri") val fotoKeluhanUri: String? = null,
    @SerializedName("fotoPerbaikanUri") val fotoPerbaikanUri: String? = null,
    @SerializedName("tanggalKeluhan") val tanggalKeluhan: String,
    @SerializedName("tanggalPerbaikan") val tanggalPerbaikan: String? = null,
    @SerializedName("STATUS") val status: String,
    @SerializedName("tanggapanAdmin") val tanggapanAdmin: String? = null
)

data class KeluhanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<KeluhanData>?
)

data class KeluhanDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: KeluhanData?
)