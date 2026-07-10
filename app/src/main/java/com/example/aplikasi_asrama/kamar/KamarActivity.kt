package com.example.aplikasi_asrama.kamar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.databinding.ActivityKamarBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class KamarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKamarBinding
    private lateinit var adapter: KamarAdapter
    private val repository = ApiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data Kamar"

        setupRecyclerView()
        setupSwipeRefresh()
        loadData()

        binding.fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahEditKamarActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = KamarAdapter(
            onEdit = { kamar ->
                startActivity(Intent(this, TambahEditKamarActivity::class.java).apply {
                    putExtra("ID", kamar.id)
                })
            },
            onDelete = { kamar ->
                hapusKamar(kamar.id)
            }
        )
        binding.rvKamar.layoutManager = LinearLayoutManager(this)
        binding.rvKamar.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getAllKamar()
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvEmpty.visibility = android.view.View.GONE
                } else {
                    binding.tvEmpty.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@KamarActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusKamar(id: Int) {
        lifecycleScope.launch {
            try {
                val success = repository.hapusKamar(id)
                if (success) {
                    Toast.makeText(this@KamarActivity, "Kamar berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@KamarActivity, "Gagal hapus kamar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@KamarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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