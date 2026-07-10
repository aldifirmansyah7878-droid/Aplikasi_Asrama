package com.example.aplikasi_asrama.helper

import java.text.SimpleDateFormat
import java.util.*

object TimeHelper {

    fun now(): Long = System.currentTimeMillis()

    fun formatTanggal(timestamp: Long): String =
        SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale("id", "ID")
        ).format(Date(timestamp))

    fun formatTanggalSingkat(timestamp: Long): String =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale("id", "ID")
        ).format(Date(timestamp))

    // ✅ Format lengkap tanggal & waktu
    fun format(timestamp: Long): String =
        SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale("id", "ID")
        ).format(Date(timestamp))

    // ✅ Format durasi dari waktu keluar & masuk
    fun formatDurasi(
        waktuKeluar: Long,
        waktuMasuk: Long
    ): String {

        val durasi = waktuMasuk - waktuKeluar

        val jam = durasi / (1000 * 60 * 60)

        val menit =
            (durasi / (1000 * 60)) % 60

        return if (jam > 0) {
            "$jam jam $menit menit"
        } else {
            "$menit menit"
        }
    }
}