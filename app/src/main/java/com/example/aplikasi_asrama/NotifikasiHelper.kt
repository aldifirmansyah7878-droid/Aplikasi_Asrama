package com.example.aplikasi_asrama.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.izin.DetailIzinActivity
import com.example.aplikasi_asrama.keluhan.KeluhanDetailActivity
import com.example.aplikasi_asrama.paket.DetailPaketActivity
import com.example.aplikasi_asrama.pembayaran.PembayaranDetailActivity
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.tamu.DetailTamuActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

object NotifikasiHelper {

    private const val CHANNEL_GLOBAL = "global_channel"
    private val repo = ApiRepository()
    private const val TAG = "NotifikasiHelper"

    // ===== BUAT CHANNEL =====
    fun buatSemuaChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_GLOBAL,
                "Notifikasi Aplikasi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi dari aplikasi"
                enableVibration(true)
                setShowBadge(true)
                setVibrationPattern(longArrayOf(0, 500, 200, 500))
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Channel notifikasi dibuat")
        }
    }

    // ===== CEK IZIN NOTIFIKASI =====
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // ===== BUKA PENGATURAN NOTIFIKASI =====
    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }

    // ===== KIRIM NOTIFIKASI (UTAMA) =====
    fun sendNotification(
        context: Context,
        userId: Int,
        judul: String,
        pesan: String,
        type: String,
        referenceId: Int = 0,
        targetClass: Class<*>? = null
    ) {
        Log.d(TAG, "sendNotification: userId=$userId, judul=$judul")

        if (userId == 0) {
            Log.e(TAG, "❌ userId = 0, tidak bisa kirim notifikasi")
            return
        }

        // Periksa izin
        val hasPermission = isNotificationPermissionGranted(context)
        Log.d(TAG, "Izin notifikasi: $hasPermission")

        // 1. Tampilkan notifikasi lokal (jika izin diberikan)
        if (hasPermission) {
            showLocalNotification(context, judul, pesan, referenceId, targetClass)
        } else {
            Log.w(TAG, "⚠️ Izin notifikasi belum diberikan, notifikasi tidak ditampilkan")
            // Bisa minta izin lagi atau buka pengaturan
        }

        // 2. Simpan ke database (background)
        CoroutineScope(Dispatchers.IO).launch {
            val success = repo.saveNotifikasi(userId, judul, pesan, type, referenceId)
            Log.d(TAG, "Save notifikasi ke DB: $success")
        }
    }

    // ===== NOTIFIKASI LOKAL =====
    private fun showLocalNotification(
        context: Context,
        judul: String,
        pesan: String,
        referenceId: Int,
        targetClass: Class<*>?
    ) {
        val intent = targetClass?.let {
            Intent(context, it).apply {
                putExtra("ID", referenceId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = intent?.let {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            PendingIntent.getActivity(context, referenceId, it, flags)
        }

        // ===== IKON NOTIFIKASI (fallback) =====
        val icon = try {
            // Coba gunakan ikon custom jika ada
            R.drawable.ic_notification
        } catch (e: Exception) {
            // Fallback ke ikon bawaan
            android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_GLOBAL)
            .setSmallIcon(icon)
            .setContentTitle(judul)
            .setContentText(pesan)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        val notifId = if (referenceId != 0) referenceId else (judul + pesan).hashCode().absoluteValue

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notifId, builder.build())
            Log.d(TAG, "✅ Notifikasi lokal berhasil ditampilkan, ID=$notifId")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException: izin notifikasi ditolak", e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal tampilkan notifikasi lokal", e)
        }
    }

    // ===== FUNGSI KHUSUS =====
    fun keluhanDiproses(context: Context, userId: Int, judulKeluhan: String, keluhanId: Int) {
        sendNotification(
            context, userId,
            "Keluhan Diproses",
            "Keluhan \"$judulKeluhan\" sedang diproses oleh admin.",
            "keluhan", keluhanId,
            KeluhanDetailActivity::class.java
        )
    }

    fun keluhanSelesai(context: Context, userId: Int, judulKeluhan: String, tanggapan: String, keluhanId: Int) {
        sendNotification(
            context, userId,
            "Keluhan Selesai",
            "Keluhan \"$judulKeluhan\" selesai.\nTanggapan: $tanggapan",
            "keluhan", keluhanId,
            KeluhanDetailActivity::class.java
        )
    }

    fun paketBaru(context: Context, userId: Int, penghuni: String, jenisPaket: String, pengirim: String, paketId: Int) {
        sendNotification(
            context, userId,
            "Paket Baru",
            "Paket $jenisPaket untuk $penghuni dari $pengirim",
            "paket", paketId,
            DetailPaketActivity::class.java
        )
    }

    fun tamuDatang(context: Context, userId: Int, namaTamu: String, tujuan: String, tamuId: Int) {
        sendNotification(
            context, userId,
            "Tamu Datang",
            "$namaTamu datang menemui $tujuan",
            "tamu", tamuId,
            DetailTamuActivity::class.java
        )
    }

    fun izinDisetujui(context: Context, userId: Int, namaPenghuni: String, izinId: Int) {
        sendNotification(
            context, userId,
            "Izin Keluar Disetujui",
            "Izin keluar untuk $namaPenghuni telah disetujui.",
            "izin", izinId,
            DetailIzinActivity::class.java
        )
    }

    fun izinDitolak(context: Context, userId: Int, namaPenghuni: String, izinId: Int) {
        sendNotification(
            context, userId,
            "Izin Keluar Ditolak",
            "Izin keluar untuk $namaPenghuni ditolak.",
            "izin", izinId,
            DetailIzinActivity::class.java
        )
    }

    fun pembayaranDiverifikasi(context: Context, userId: Int, penghuni: String, jumlah: Int, pembayaranId: Int) {
        sendNotification(
            context, userId,
            "Pembayaran Diverifikasi",
            "Pembayaran Rp$jumlah untuk $penghuni telah diverifikasi.",
            "pembayaran", pembayaranId,
            PembayaranDetailActivity::class.java
        )
    }

    fun pembayaranDitolak(context: Context, userId: Int, penghuni: String, jumlah: Int, pembayaranId: Int) {
        sendNotification(
            context, userId,
            "Pembayaran Ditolak",
            "Pembayaran Rp$jumlah untuk $penghuni ditolak.",
            "pembayaran", pembayaranId,
            PembayaranDetailActivity::class.java
        )
    }

    // ===== TEST NOTIFIKASI =====
    fun testNotifikasi(context: Context, userId: Int) {
        sendNotification(
            context, userId,
            "🔔 Notifikasi Test",
            "Ini adalah notifikasi percobaan. Jika Anda melihat ini, notifikasi berfungsi!",
            "test", 9999
        )
    }

    // ===== MARK NOTIFIKASI SUDAH DIBACA =====
    fun markAsRead(context: Context, notifikasiId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = repo.markReadNotifikasi(notifikasiId)
            if (success) {
                Log.d(TAG, "✅ Notifikasi ID $notifikasiId ditandai sudah dibaca")
            } else {
                Log.e(TAG, "❌ Gagal menandai notifikasi ID $notifikasiId")
            }
        }
    }
}