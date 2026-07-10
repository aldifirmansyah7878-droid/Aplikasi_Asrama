package com.example.aplikasi_asrama.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.RetrofitClient
import com.example.aplikasi_asrama.api.model.TrackingRequest
import com.example.aplikasi_asrama.repository.ApiRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val TAG = "TrackingService"
    private val repository = ApiRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var izinId: Int = 0
    private var userId: Int = 0
    private var isRunning = false
    private var isFirstUpdate = true

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { location ->
                sendLocation(location)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        izinId = intent?.getIntExtra("izin_id", 0) ?: 0
        userId = intent?.getIntExtra("user_id", 0) ?: 0

        Log.d(TAG, "onStartCommand: izinId=$izinId, userId=$userId")

        if (izinId == 0 || userId == 0) {
            Log.e(TAG, "izin_id atau user_id tidak valid, stop service")
            stopSelf()
            return START_NOT_STICKY
        }

        isRunning = true
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (!checkLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 detik
            fastestInterval = 5000
            smallestDisplacement = 10f // 10 meter
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            Log.e(TAG, "Failed to request location updates", e)
        }

        // Ambil lokasi terakhir segera
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && isFirstUpdate) {
                isFirstUpdate = false
                sendLocation(location)
            }
        }
    }

    private fun sendLocation(location: Location) {
        if (!isRunning) return
        Log.d(TAG, "Sending location: ${location.latitude}, ${location.longitude}")

        val request = TrackingRequest(
            izin_id = izinId,
            user_id = userId,
            latitude = location.latitude,
            longitude = location.longitude,
            alamat = ""
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = repository.sendTracking(request)
                if (success) {
                    Log.d(TAG, "Location sent successfully")
                } else {
                    Log.e(TAG, "Failed to send location")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending location", e)
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ========== NOTIFICATION ==========
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracking Lokasi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi saat tracking lokasi berjalan"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Lokasi Aktif")
            .setContentText("Mengirim lokasi untuk izin keluar...")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, izinId: Int, userId: Int) {
            val intent = Intent(context, TrackingService::class.java).apply {
                putExtra("izin_id", izinId)
                putExtra("user_id", userId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d("TrackingService", "start called: izinId=$izinId, userId=$userId")
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
            Log.d("TrackingService", "stop called")
        }
    }
}