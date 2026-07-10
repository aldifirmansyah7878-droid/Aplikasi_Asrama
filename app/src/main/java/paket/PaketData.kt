package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class PaketData(
    @SerializedName("id") val id: Int,
    @SerializedName("userid") val userid: Int,
    @SerializedName("namaPenghuni") val namaPenghuni: String,
    @SerializedName("nomorKamar") val nomorKamar: String,
    @SerializedName("namaPengirim") val namaPengirim: String,
    @SerializedName("jenisPaket") val jenisPaket: String,
    @SerializedName("tanggalDatang") val tanggalDatang: String,
    @SerializedName("STATUS") val status: String,
    @SerializedName("fotoUri") val fotoUri: String?
)

data class PaketResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<PaketData>?
)

data class UpdateStatusRequest(
    val id: Int,
    val status: String
)

data class HapusPaketRequest(
    val id: Int
)

