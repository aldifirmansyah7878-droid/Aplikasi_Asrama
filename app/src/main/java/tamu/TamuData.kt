package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class TamuData(
    @SerializedName("id") val id: Int,
    @SerializedName("namaTamu") val namaTamu: String,
    @SerializedName("tujuanPenghuni") val tujuanPenghuni: String,
    @SerializedName("nomorKamar") val nomorKamar: String,
    @SerializedName("waktuKunjungan") val waktuKunjungan: String,
    @SerializedName("hubungan") val hubungan: String,
    @SerializedName("STATUS") val status: String,
    @SerializedName("userid") val userid: Int  // <-- pakai huruf kecil
)

data class TamuResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<TamuData>?
)

data class TamuDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: TamuData?
)

data class TamuRequest(
    @SerializedName("namaTamu") val namaTamu: String,
    @SerializedName("tujuanPenghuni") val tujuanPenghuni: String,
    @SerializedName("nomorKamar") val nomorKamar: String,
    @SerializedName("waktuKunjungan") val waktuKunjungan: String,
    @SerializedName("hubungan") val hubungan: String,
    @SerializedName("userid") val userid: Int
)