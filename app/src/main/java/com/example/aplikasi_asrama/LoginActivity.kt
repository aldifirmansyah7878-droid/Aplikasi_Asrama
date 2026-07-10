package com.example.aplikasi_asrama

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasi_asrama.databinding.ActivityLoginBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.viewmodel.LoginResult
import com.example.aplikasi_asrama.viewmodel.LoginViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private val isNavigating = AtomicBoolean(false)
    private var loginJob: Job? = null

    companion object {
        const val PREFS_NAME = "user_pref"
        const val KEY_USER_ID = "userId"
        const val KEY_ROLE = "role"
        const val KEY_USERNAME = "username"
        const val KEY_NAMA_LENGKAP = "nama_lengkap"
        const val KEY_PENGHUNI_ID = "penghuni_id"
        private const val TAG = "LoginActivity"

        // Fungsi logout global dengan finishAffinity()
        fun logout(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d(TAG, "Logout: session cleared")
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            // Tutup semua activity agar benar-benar logout
            if (context is AppCompatActivity) {
                context.finishAffinity()
            }
        }
    }

    private val permissionNotifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notifikasi diizinkan")
        } else {
            Log.w(TAG, "Notifikasi ditolak")
            Toast.makeText(this, "Izin notifikasi diperlukan untuk menerima pemberitahuan", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Buat channel notifikasi
        NotifikasiHelper.buatSemuaChannel(this)
        mintaPermissionNotifikasi()

        // Back gesture handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.progressBar.visibility == View.VISIBLE) {
                    Toast.makeText(this@LoginActivity, "Sedang memproses...", Toast.LENGTH_SHORT).show()
                    return
                }
                finish()
            }
        })

        // Cek auto login
        if (checkAutoLogin()) {
            return
        }

        observeLoginResult()

        binding.btnLogin.setOnClickListener {
            if (isNavigating.get()) return@setOnClickListener
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginJob?.cancel()
            loginJob = lifecycleScope.launch {
                viewModel.login(username, password)
            }
        }

        binding.tvDaftar.setOnClickListener {
            if (isNavigating.get()) return@setOnClickListener
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset flag navigasi jika kembali ke LoginActivity
        if (isNavigating.get()) {
            isNavigating.set(false)
            Log.d(TAG, "onResume: reset isNavigating")
        }
    }

    private fun checkAutoLogin(): Boolean {
        if (isNavigating.get()) return true
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getInt(KEY_USER_ID, 0)
        val role = prefs.getString(KEY_ROLE, "")?.trim() ?: ""

        Log.d(TAG, "checkAutoLogin: userId=$userId, role='$role'")

        if (userId != 0 && role.isNotEmpty()) {
            if (role == "user") {
                lifecycleScope.launch {
                    try {
                        val repo = ApiRepository()
                        val penghuni = repo.getPenghuniByUserId(userId)
                        if (penghuni != null) {
                            prefs.edit().putInt(KEY_PENGHUNI_ID, penghuni.id).apply()
                            Log.d(TAG, "Auto-login: penghuni_id = ${penghuni.id}")
                        } else {
                            Log.w(TAG, "Auto-login: penghuni tidak ditemukan untuk userId=$userId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto-login gagal simpan penghuni_id", e)
                    } finally {
                        if (!isNavigating.getAndSet(true)) {
                            navigateToDashboard(role, userId)
                        }
                    }
                }
            } else {
                if (!isNavigating.getAndSet(true)) {
                    navigateToDashboard(role, userId)
                }
            }
            return true
        }
        return false
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            if (isNavigating.get() || isFinishing || isDestroyed) return@observe

            when (result) {
                is LoginResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is LoginResult.Success -> {
                    viewModel.loginResult.removeObservers(this)
                    if (isNavigating.get()) return@observe

                    val userId = result.user.id
                    val role = result.user.role?.trim() ?: ""

                    Log.d(TAG, "Login sukses: userId=$userId, role='$role'")

                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().apply {
                        putInt(KEY_USER_ID, userId)
                        putString(KEY_USERNAME, result.user.username?.trim() ?: "")
                        putString(KEY_NAMA_LENGKAP, result.user.namaLengkap?.trim() ?: "")
                        putString(KEY_ROLE, role)
                        apply()
                    }

                    Log.d(TAG, "SharedPreferences setelah simpan: userId=${prefs.getInt(KEY_USER_ID, 0)}, role=${prefs.getString(KEY_ROLE, "")}")

                    if (role == "user") {
                        lifecycleScope.launch {
                            try {
                                val repo = ApiRepository()
                                val penghuni = repo.getPenghuniByUserId(userId)
                                if (penghuni != null) {
                                    prefs.edit().putInt(KEY_PENGHUNI_ID, penghuni.id).apply()
                                    Log.d(TAG, "penghuni_id = ${penghuni.id}")
                                } else {
                                    Log.w(TAG, "Penghuni tidak ditemukan untuk userId=$userId")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Gagal simpan penghuni_id", e)
                            } finally {
                                if (!isNavigating.getAndSet(true)) {
                                    navigateToDashboard(role, userId)
                                }
                            }
                        }
                    } else {
                        if (!isNavigating.getAndSet(true)) {
                            navigateToDashboard(role, userId)
                        }
                    }
                }
                is LoginResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = result.message
                    isNavigating.set(false)
                }
            }
        }
    }

    private fun navigateToDashboard(role: String, userId: Int) {
        Log.d(TAG, "navigateToDashboard: role='$role', userId=$userId")

        val cleanRole = role.trim()
        if (cleanRole.isEmpty()) {
            Log.e(TAG, "Role kosong, tidak bisa navigasi")
            isNavigating.set(false)
            return
        }

        val targetClass = when (cleanRole) {
            "admin" -> MainActivity::class.java
            "user" -> UserDashboardActivity::class.java
            else -> {
                Log.e(TAG, "Role tidak dikenal: '$cleanRole'")
                isNavigating.set(false)
                LoginActivity::class.java
            }
        }

        Log.d(TAG, "Navigasi ke: ${targetClass.simpleName}")

        val intent = Intent(this, targetClass)
        if (cleanRole == "user") {
            intent.putExtra(KEY_USER_ID, userId)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // =============================================
    // FUNGSI CLEAR SESSION (untuk logout)
    // =============================================
    fun clearSession() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d(TAG, "Session cleared")
    }

    private fun mintaPermissionNotifikasi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(TAG, "Notifikasi sudah diizinkan")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginJob?.cancel()
        viewModel.loginResult.removeObservers(this)
    }
}