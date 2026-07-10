package com.example.aplikasi_asrama.paket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityPaketUserBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class PaketUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaketUserBinding
    private lateinit var adapter: PaketUserAdapter
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaketUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paket Saya"

        if (userId == 0) {
            Toast.makeText(this, "User tidak valid. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = PaketUserAdapter { paket ->
            val intent = Intent(this, DetailPaketActivity::class.java)
            intent.putExtra("PAKET_ID", paket.id)
            intent.putExtra("FROM_USER", true)
            startActivity(intent)
        }
        binding.rvPaket.layoutManager = LinearLayoutManager(this)
        binding.rvPaket.adapter = adapter
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getPaketByUser(userId)
                binding.progressBar.visibility = View.GONE
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvKosong.visibility = View.GONE
                } else {
                    binding.tvKosong.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@PaketUserActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}