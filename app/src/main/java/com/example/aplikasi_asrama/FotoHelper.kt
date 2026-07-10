package com.example.aplikasi_asrama.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FotoHelper {

    private const val MAX_SIZE = 1024
    private const val QUALITY  = 80

    // Kompres & simpan ke internal storage
    fun simpanFoto(context: Context, uri: Uri, subfolder: String = "foto_keluhan"): String? {
        return try {
            val input  = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()

            val ratio = minOf(
                MAX_SIZE.toFloat() / bitmap.width,
                MAX_SIZE.toFloat() / bitmap.height
            )

            val w = if (ratio < 1)
                (bitmap.width * ratio).toInt()
            else
                bitmap.width

            val h = if (ratio < 1)
                (bitmap.height * ratio).toInt()
            else
                bitmap.height

            val resized = Bitmap.createScaledBitmap(bitmap, w, h, true)

            bitmap.recycle()

            val folder = File(context.filesDir, subfolder)
                .also { it.mkdirs() }

            val file = File(
                folder,
                "foto_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(file).use { out ->
                resized.compress(
                    Bitmap.CompressFormat.JPEG,
                    QUALITY,
                    out
                )
            }

            resized.recycle()

            file.absolutePath

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }

    // Cek ukuran file MB
    fun cekUkuranMB(context: Context, uri: Uri): Double {

        val c = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )

        c?.use {

            val i = it.getColumnIndex(
                android.provider.OpenableColumns.SIZE
            )

            it.moveToFirst()

            return it.getLong(i) / (1024.0 * 1024.0)
        }

        return 0.0
    }

    // ✅ Tambahkan ini
    fun cekUkuranFile(
        context: Context,
        uri: Uri
    ): Double {
        return cekUkuranMB(context, uri)
    }

    // ✅ Tambahkan ini
    fun simpanFotoDariUri(
        context: Context,
        uri: Uri
    ): String? {
        return simpanFoto(context, uri)
    }
}