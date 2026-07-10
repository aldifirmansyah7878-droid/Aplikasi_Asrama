package com.example.aplikasi_asrama.tamu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityTamuUserBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class TamuUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTamuUserBinding
    private lateinit var adapter: TamuUserAdapter
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val TAG = "TamuUserActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTamuUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Tamu"

        if (userId == 0) {
            Toast.makeText(this, "User tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "userId: $userId")

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // ===== ADAPTER DENGAN CLICK LISTENER =====
        adapter = TamuUserAdapter { tamu ->
            val intent = Intent(this, DetailTamuActivity::class.java)
            intent.putExtra("TAMU_ID", tamu.id)
            intent.putExtra("FROM_USER", true)  // tandai dari user
            startActivity(intent)
        }
        binding.rvTamu.layoutManager = LinearLayoutManager(this)
        binding.rvTamu.adapter = adapter
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Memuat data tamu untuk userid: $userId")
                val data = repository.getTamuByUser(userId)
                Log.d(TAG, "Data tamu: ${data.size} item")
                binding.progressBar.visibility = View.GONE
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvKosong.visibility = View.GONE
                } else {
                    binding.tvKosong.visibility = View.VISIBLE
                    binding.tvKosong.text = "Belum ada kunjungan tamu"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "loadData error", e)
                Toast.makeText(this@TamuUserActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()  // refresh otomatis
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}