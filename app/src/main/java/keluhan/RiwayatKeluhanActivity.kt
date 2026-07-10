package com.example.aplikasi_asrama.keluhan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityRiwayatKeluhanBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class RiwayatKeluhanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatKeluhanBinding
    private lateinit var adapter: KeluhanUserAdapter
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatKeluhanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Keluhan"

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = KeluhanUserAdapter { keluhan ->
            KeluhanDetailActivity.start(this, keluhan.id)
        }
        binding.rvRiwayatKeluhan.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatKeluhan.adapter = adapter
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getKeluhanByUser(userId)
                binding.progressBar.visibility = View.GONE
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvKosong.visibility = View.GONE
                } else {
                    adapter.submitList(emptyList())
                    binding.tvKosong.visibility = View.VISIBLE
                    binding.tvKosong.text = "Belum ada keluhan yang dikirim"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@RiwayatKeluhanActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}