package com.example.aplikasi_asrama.keluhan

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.databinding.ActivityKeluhanAdminBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class KeluhanAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeluhanAdminBinding
    private lateinit var adapter: KeluhanAdminAdapter
    private val repository = ApiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeluhanAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Keluhan Masuk"

        setupRecyclerView()
        setupFilter()
        loadData(null)

        binding.btnRefresh.setOnClickListener {
            val status = binding.spinnerFilter.selectedItem.toString()
            loadData(if (status == "Semua") null else status)
        }
    }

    private fun setupRecyclerView() {
        adapter = KeluhanAdminAdapter(
            onDetail = { keluhan ->
                KeluhanDetailActivity.start(this, keluhan.id)
            },
            onTanggapan = { keluhan ->
                // Bisa dibuka ke halaman tanggapan
                KeluhanDetailActivity.start(this, keluhan.id)
            },
            onHapus = { keluhan ->
                // Hapus keluhan
                hapusKeluhan(keluhan.id)
            }
        )
        binding.rvKeluhan.layoutManager = LinearLayoutManager(this)
        binding.rvKeluhan.adapter = adapter
    }

    private fun setupFilter() {
        val items = arrayOf("Semua", "menunggu", "proses", "selesai")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = parent?.getItemAtPosition(position).toString()
                loadData(if (status == "Semua") null else status)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadData(status: String?) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getAllKeluhan(status)
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
                Toast.makeText(this@KeluhanAdminActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusKeluhan(id: Int) {
        lifecycleScope.launch {
            try {
                val success = repository.hapusKeluhan(id)
                if (success) {
                    Toast.makeText(this@KeluhanAdminActivity, "Keluhan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadData(null)
                } else {
                    Toast.makeText(this@KeluhanAdminActivity, "Gagal menghapus keluhan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@KeluhanAdminActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}