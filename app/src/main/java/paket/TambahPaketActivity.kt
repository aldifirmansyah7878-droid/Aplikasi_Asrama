package com.example.aplikasi_asrama.paket

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.databinding.ActivityTambahPaketBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class TambahPaketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahPaketBinding
    private val repository = ApiRepository()
    private val TAG = "TambahPaket"

    private var selectedPhotoFile: File? = null
    private var penghuniList: List<PenghuniData> = emptyList()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedPhotoFile = getFileFromUri(uri)
                binding.tvNamaFoto.text = selectedPhotoFile?.name ?: "Foto dipilih"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahPaketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tambah Paket"

        loadPenghuni()
        setupSpinnerStatus()
        setupDatePicker()

        binding.btnPilihFoto.setOnClickListener {
            pilihFoto()
        }

        binding.btnHapusFoto.setOnClickListener {
            selectedPhotoFile = null
            binding.tvNamaFoto.text = "Belum ada foto"
        }

        binding.btnSimpan.setOnClickListener {
            simpanPaket()
        }
    }

    private fun loadPenghuni() {
        lifecycleScope.launch {
            try {
                penghuniList = repository.getAllPenghuni()
                val namaList = penghuniList.map { "${it.nama} - Kamar ${it.nomorKamar}" }
                val adapter = ArrayAdapter(
                    this@TambahPaketActivity,
                    android.R.layout.simple_spinner_item,
                    namaList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerPenghuni.adapter = adapter
            } catch (e: Exception) {
                Log.e(TAG, "loadPenghuni error", e)
                Toast.makeText(this@TambahPaketActivity, "Gagal memuat data penghuni", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinnerStatus() {
        val statusList = arrayOf("menunggu", "diambil", "selesai")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etTanggalDatang.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    binding.etTanggalDatang.setText(date)
                },
                year, month, day
            ).show()
        }
    }

    private fun pilihFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                it.moveToFirst()
                val path = it.getString(columnIndex)
                File(path)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFileFromUri error", e)
            null
        }
    }

    private fun simpanPaket() {
        val position = binding.spinnerPenghuni.selectedItemPosition
        if (position < 0 || position >= penghuniList.size) {
            Toast.makeText(this, "Pilih penghuni terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        val penghuni = penghuniList[position]
        val namaPengirim = binding.etNamaPengirim.text.toString().trim()
        val jenisPaket = binding.etJenisPaket.text.toString().trim()
        val tanggalDatang = binding.etTanggalDatang.text.toString().trim()
        val status = binding.spinnerStatus.selectedItem.toString()

        if (namaPengirim.isEmpty() || jenisPaket.isEmpty() || tanggalDatang.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoFile == null) {
            Toast.makeText(this, "Foto paket wajib dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        // Log untuk debugging
        Log.d(TAG, "Mengirim: userid=${penghuni.userId}, namaPenghuni=${penghuni.nama}, ...")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = repository.tambahPaket(
                    userId = penghuni.userId,
                    namaPenghuni = penghuni.nama,
                    nomorKamar = penghuni.nomorKamar ?: "",
                    namaPengirim = namaPengirim,
                    jenisPaket = jenisPaket,
                    tanggalDatang = tanggalDatang,
                    status = status,
                    fotoFile = selectedPhotoFile
                )
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                if (success) {
                    Toast.makeText(this@TambahPaketActivity, "Paket berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahPaketActivity, "Gagal menambahkan paket", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Log.e(TAG, "simpanPaket error", e)
                Toast.makeText(this@TambahPaketActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}