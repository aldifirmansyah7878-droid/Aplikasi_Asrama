package com.example.aplikasi_asrama.kamar

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.KamarData
import com.example.aplikasi_asrama.databinding.ActivityTambahEditKamarBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahEditKamarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahEditKamarBinding
    private val repository = ApiRepository()
    private var kamarId: Int = 0
    private val TAG = "TambahEditKamar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahEditKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        kamarId = intent.getIntExtra("ID", 0)

        if (kamarId > 0) {
            supportActionBar?.title = "Edit Kamar"
            loadKamarData()
        } else {
            supportActionBar?.title = "Tambah Kamar"
        }

        setupStatusSpinner()
        setupDatePicker()

        binding.btnSimpan.setOnClickListener {
            simpanKamar()
        }
    }

    private fun setupStatusSpinner() {
        val statusList = listOf("Tersedia", "Penuh", "Rusak")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance()
                date.set(selectedYear, selectedMonth, selectedDay)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.etTanggal.setText(format.format(date.time))
            }, year, month, day).show()
        }
    }

    private fun loadKamarData() {
        lifecycleScope.launch {
            try {
                val kamar = repository.getKamarById(kamarId)
                if (kamar != null) {
                    binding.etNomorKamar.setText(kamar.nomorKamar)
                    binding.etLantai.setText(kamar.lantai.toString())
                    binding.etKapasitas.setText(kamar.kapasitas.toString())
                    binding.etHarga.setText(kamar.hargaPerBulan.toString())
                    binding.etFasilitas.setText(kamar.fasilitas ?: "")
                    binding.etTanggal.setText(kamar.tanggal ?: "")

                    val statusPos = listOf("Tersedia", "Penuh", "Rusak").indexOf(kamar.status)
                    if (statusPos >= 0) binding.spinnerStatus.setSelection(statusPos)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal load data", e)
                Toast.makeText(this@TambahEditKamarActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simpanKamar() {
        val nomorKamar = binding.etNomorKamar.text.toString().trim()
        val lantaiStr = binding.etLantai.text.toString().trim()
        val kapasitasStr = binding.etKapasitas.text.toString().trim()
        val hargaStr = binding.etHarga.text.toString().trim()
        val fasilitas = binding.etFasilitas.text.toString().trim()
        val status = binding.spinnerStatus.selectedItem.toString()
        val tanggal = binding.etTanggal.text.toString().trim()

        // Validasi
        if (nomorKamar.isEmpty() || lantaiStr.isEmpty() || kapasitasStr.isEmpty() || hargaStr.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi (termasuk tanggal)", Toast.LENGTH_SHORT).show()
            return
        }

        val lantai = lantaiStr.toIntOrNull()
        val kapasitas = kapasitasStr.toIntOrNull()
        val harga = hargaStr.toIntOrNull()

        if (lantai == null || kapasitas == null || harga == null || lantai <= 0 || kapasitas <= 0 || harga <= 0) {
            Toast.makeText(this, "Isi angka yang valid untuk lantai, kapasitas, dan harga", Toast.LENGTH_SHORT).show()
            return
        }

        val kamar = KamarData(
            id = kamarId,
            nomorKamar = nomorKamar,
            lantai = lantai,
            kapasitas = kapasitas,
            hargaPerBulan = harga,
            fasilitas = fasilitas.ifEmpty { null },
            status = status,
            tanggal = tanggal
        )

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = if (kamarId > 0) {
                    repository.editKamar(kamar)
                } else {
                    repository.tambahKamar(kamar)
                }
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSimpan.isEnabled = true
                if (success) {
                    Toast.makeText(this@TambahEditKamarActivity, "Kamar berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahEditKamarActivity, "Gagal menyimpan kamar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSimpan.isEnabled = true
                Log.e(TAG, "Error simpan kamar", e)
                Toast.makeText(this@TambahEditKamarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}