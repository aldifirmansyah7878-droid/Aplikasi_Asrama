package com.example.aplikasi_asrama.notifikasi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityNotifikasiBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class NotifikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotifikasiBinding
    private lateinit var adapter: NotifikasiAdapter
    private val repo = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notifikasi"

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // ===== ADAPTER DENGAN CLICK LISTENER =====
        adapter = NotifikasiAdapter { notifikasi ->
            // Tandai sebagai dibaca jika belum
            if (notifikasi.isRead == 0) {
                lifecycleScope.launch {
                    val success = repo.markReadNotifikasi(notifikasi.id)
                    if (success) {
                        // Refresh data
                        loadData()
                    } else {
                        Toast.makeText(this@NotifikasiActivity, "Gagal menandai dibaca", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // Buka detail sesuai type (opsional)
            // navigateToDetail(notifikasi)
        }
        binding.rvNotifikasi.layoutManager = LinearLayoutManager(this)
        binding.rvNotifikasi.adapter = adapter
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repo.getNotifikasi(userId)
                binding.progressBar.visibility = View.GONE
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvKosong.visibility = View.GONE
                } else {
                    adapter.submitList(emptyList())
                    binding.tvKosong.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvKosong.visibility = View.VISIBLE
                binding.tvKosong.text = "Gagal memuat data: ${e.message}"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}