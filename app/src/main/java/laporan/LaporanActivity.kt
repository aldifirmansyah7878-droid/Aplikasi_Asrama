package com.example.aplikasi_asrama.laporan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.databinding.ActivityLaporanBinding
import com.example.aplikasi_asrama.viewmodel.LaporanViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class Laporan : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanBinding
    private lateinit var viewModel: LaporanViewModel
    private lateinit var adapter: LaporanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[LaporanViewModel::class.java]

        setupSpinners()
        setupRecyclerView()
        observeData()

        binding.btnFilter.setOnClickListener {
            val tahun = binding.spinnerTahun.selectedItem.toString()
            val bulan = binding.spinnerBulan.selectedItem.toString()
            val selectedTahun = if (tahun == "Semua Tahun") null else tahun
            val selectedBulan = if (bulan == "Semua Bulan") null else bulan
            viewModel.loadLaporan(selectedTahun, selectedBulan)
        }

        viewModel.loadLaporan(null, null) // Load semua data awal
    }

    private fun setupSpinners() {
        // Tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf("Semua Tahun")
        for (i in -2..2) years.add((currentYear + i).toString())
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTahun.adapter = tahunAdapter
        binding.spinnerTahun.setSelection(years.indexOf(currentYear.toString()).takeIf { it >= 0 } ?: 1)

        // Bulan
        val months = listOf(
            "Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val bulanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBulan.adapter = bulanAdapter
    }

    private fun setupRecyclerView() {
        adapter = LaporanAdapter()
        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter
    }

    private fun observeData() {
        viewModel.laporanList.observe(this) { list ->
            adapter.submitList(list)
            binding.tvKosong.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        viewModel.total.observe(this) { total ->
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            binding.tvTotal.text = "Rp ${formatter.format(total)}"
        }
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}