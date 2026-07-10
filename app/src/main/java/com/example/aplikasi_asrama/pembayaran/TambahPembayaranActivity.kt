package com.example.aplikasi_asrama.pembayaran

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.databinding.ActivityTambahPembayaranBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahPembayaranBinding
    private val repository = ApiRepository()
    private var daftarPenghuni: List<PenghuniData> = emptyList()
    private var selectedPenghuni: PenghuniData? = null
    private val TAG = "TambahPembayaran"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        loadPenghuniSpinner()
        setupBulanTahunSpinner()
        setupMetodeSpinner()

        binding.btnSimpan.setOnClickListener {
            simpanPembayaran()
        }
    }

    private fun loadPenghuniSpinner() {
        binding.progressOverlay.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                daftarPenghuni = repository.getAllPenghuni()
                if (daftarPenghuni.isNotEmpty()) {
                    val adapter = ArrayAdapter(
                        this@TambahPembayaranActivity,
                        R.layout.item_spinner_penghuni,  // custom layout (opsional)
                        daftarPenghuni.map { "${it.nama} (Kamar ${it.nomorKamar})" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPenghuni.adapter = adapter

                    selectedPenghuni = daftarPenghuni.first()
                    binding.spinnerPenghuni.setSelection(0)

                    binding.spinnerPenghuni.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (position >= 0 && position < daftarPenghuni.size) {
                                selectedPenghuni = daftarPenghuni[position]
                            }
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                } else {
                    Toast.makeText(this@TambahPembayaranActivity, "Tidak ada data penghuni", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal load penghuni", e)
                Toast.makeText(this@TambahPembayaranActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressOverlay.visibility = View.GONE
            }
        }
    }

    private fun setupBulanTahunSpinner() {
        val months = getLast12Months()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, months)
        binding.autoCompleteBulanTahun.setAdapter(adapter)
        if (months.isNotEmpty()) binding.autoCompleteBulanTahun.setText(months.last(), false)
    }

    private fun setupMetodeSpinner() {
        val metodeList = listOf("Transfer Bank", "Tunai", "QRIS", "Virtual Account", "E-Wallet")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, metodeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMetode.adapter = adapter
    }

    private fun getLast12Months(): List<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val months = mutableListOf<String>()
        for (i in 0 until 12) {
            months.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, -1)
        }
        return months.reversed()
    }

    private fun simpanPembayaran() {
        val bulanTahun = binding.autoCompleteBulanTahun.text.toString().trim()
        val jumlah = binding.etJumlah.text.toString().trim()
        val metode = binding.spinnerMetode.selectedItem.toString()

        if (selectedPenghuni == null) {
            Toast.makeText(this, "Pilih penghuni terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (bulanTahun.isEmpty() || jumlah.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlahInt = jumlah.replace(Regex("[^0-9]"), "").toIntOrNull()
        if (jumlahInt == null || jumlahInt <= 0) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan progress overlay
        binding.progressOverlay.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = repository.tambahPembayaranAdmin(
                    penghuni_Id = selectedPenghuni!!.id,
                    bulanTahun = bulanTahun,
                    jumlah = jumlahInt,
                    metode = metode
                )
                binding.progressOverlay.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                if (success) {
                    Toast.makeText(this@TambahPembayaranActivity, "Pembayaran berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahPembayaranActivity, "Gagal simpan pembayaran. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressOverlay.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Log.e(TAG, "Error simpan", e)
                Toast.makeText(this@TambahPembayaranActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}