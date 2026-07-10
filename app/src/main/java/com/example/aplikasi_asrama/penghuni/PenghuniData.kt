package com.example.aplikasi_asrama

import com.google.gson.annotations.SerializedName

data class PenghuniData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("nama") val nama: String,
    @SerializedName("nim") val Nim: String,
    @SerializedName("no_telp") val noTelp: String,
    @SerializedName("alamat_asal") val alamatAsal: String,
    @SerializedName("kamar_id") val kamarId: Int,
    @SerializedName("nomor_kamar") val nomorKamar: String,
    @SerializedName("tanggal_masuk") val tanggalMasuk: String,
    @SerializedName("tanggal_keluar") val tanggalKeluar: String?,
    @SerializedName("STATUS") val status: String,
    @SerializedName("foto_uri") val fotoUri: String?
)