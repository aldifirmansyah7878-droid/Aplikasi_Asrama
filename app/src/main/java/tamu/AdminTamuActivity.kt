package com.example.aplikasi_asrama.tamu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.api.model.TamuData
import com.example.aplikasi_asrama.databinding.ActivityAdminTamuBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper  // <-- TAMBAHKAN
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class AdminTamuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminTamuBinding
    private lateinit var adapter: TamuAdminAdapter
    private val repository = ApiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTamuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data Tamu"

        setupRecyclerView()
        setupFilter()
        loadData(null)

        binding.btnRefresh.setOnClickListener {
            val status = binding.spinnerFilter.selectedItem.toString()
            loadData(if (status == "Semua") null else status)
        }
    }

    private fun setupRecyclerView() {
        adapter = TamuAdminAdapter(
            onStatusUpdate = { tamu, status ->
                updateStatus(tamu.id, status, tamu)
            },
            onHapus = { tamu ->
                hapusTamu(tamu.id)
            }
        )
        binding.rvTamu.layoutManager = LinearLayoutManager(this)
        binding.rvTamu.adapter = adapter
    }

    private fun setupFilter() {
        val items = arrayOf("Semua", "menunggu", "disetujui", "ditolak", "selesai")
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
                val data = repository.getAllTamu(status)
                Log.d("AdminTamu", "Data tamu: ${data.size}")
                binding.progressBar.visibility = View.GONE
                if (data.isNotEmpty()) {
                    adapter.submitList(data)
                    binding.tvKosong.visibility = View.GONE
                } else {
                    binding.tvKosong.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminTamuActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatus(id: Int, status: String, tamu: TamuData) {
        lifecycleScope.launch {
            try {
                val success = repository.updateStatusTamu(id, status)
                if (success) {
                    Toast.makeText(this@AdminTamuActivity, "Status berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadData(null)

                    // ===== KIRIM NOTIFIKASI =====
                    if (status == "disetujui") {
                        NotifikasiHelper.tamuDatang(
                            this@AdminTamuActivity,
                            tamu.userid,
                            tamu.namaTamu,
                            tamu.tujuanPenghuni,
                            tamu.id
                        )
                    }
                } else {
                    Toast.makeText(this@AdminTamuActivity, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusTamu(id: Int) {
        lifecycleScope.launch {
            try {
                val success = repository.hapusTamu(id)
                if (success) {
                    Toast.makeText(this@AdminTamuActivity, "Tamu berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadData(null)
                } else {
                    Toast.makeText(this@AdminTamuActivity, "Gagal hapus tamu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}