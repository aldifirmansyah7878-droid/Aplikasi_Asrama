package com.example.aplikasi_asrama

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasi_asrama.databinding.ActivityMainBinding
import com.example.aplikasi_asrama.izin.IzinAdminActivity
import com.example.aplikasi_asrama.kamar.KamarActivity
import com.example.aplikasi_asrama.keluhan.KeluhanAdminActivity
import com.example.aplikasi_asrama.notifikasi.NotifikasiActivity
import com.example.aplikasi_asrama.paket.AdminPaketActivity
import com.example.aplikasi_asrama.pembayaran.PembayaranAdminActivity
import com.example.aplikasi_asrama.penghuni.PenghuniActivity
import com.example.aplikasi_asrama.tamu.AdminTamuActivity
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.viewmodel.DashboardViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dashboardViewModel: DashboardViewModel
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val repository = ApiRepository()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
        }
        dashboardViewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)

        // Ambil username dari SharedPreferences
        val username = prefs.getString(LoginActivity.KEY_USERNAME, "Admin") ?: "Admin"
        binding.tvWelcome.text = "Halo, $username"

        // Observasi data dashboard
        dashboardViewModel.dashboardData.observe(this) { data ->
            if (data != null) {
                binding.tvTotalPenghuni.text = data.totalPenghuni.toString()
                binding.tvTotalKamar.text = data.totalKamar.toString()
                binding.tvKamarTersedia.text = data.kamarTersedia.toString()
                binding.tvTotalPendapatan.text = data.getFormattedPendapatan()
            }
        }

        dashboardViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        dashboardViewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Load data dashboard
        dashboardViewModel.loadDashboard()

        // Navigasi menu
        binding.cardPenghuni.setOnClickListener { startActivity(Intent(this, PenghuniActivity::class.java)) }
        binding.cardKamar.setOnClickListener { startActivity(Intent(this, KamarActivity::class.java)) }
        binding.cardPembayaran.setOnClickListener { startActivity(Intent(this, PembayaranAdminActivity::class.java)) }
        binding.cardIzin.setOnClickListener { startActivity(Intent(this, IzinAdminActivity::class.java)) }
        binding.cardKeluhan.setOnClickListener { startActivity(Intent(this, KeluhanAdminActivity::class.java)) }
        binding.cardPaket.setOnClickListener { startActivity(Intent(this, AdminPaketActivity::class.java)) }
        binding.cardTamu.setOnClickListener { startActivity(Intent(this, AdminTamuActivity::class.java)) }

        // Logout
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    prefs.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    // ===== MENU NOTIFIKASI =====
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifikasi -> {
                startActivity(Intent(this, NotifikasiActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.loadDashboard()
    }
}