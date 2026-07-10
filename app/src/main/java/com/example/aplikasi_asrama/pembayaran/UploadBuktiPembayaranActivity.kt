package com.example.aplikasi_asrama.pembayaran

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
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityUploadBuktiPembayaranBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class UploadBuktiPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBuktiPembayaranBinding
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val penghuniId by lazy { prefs.getInt(LoginActivity.KEY_PENGHUNI_ID, 0) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    private var selectedFile: File? = null
    private val TAG = "UploadBukti"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedFile = getFileFromUri(uri)
                if (selectedFile != null) {
                    binding.tvNamaFile.text = selectedFile?.name ?: "File dipilih"
                    binding.btnHapus.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Gagal memilih file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBuktiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Upload Bukti"

        if (penghuniId == 0) {
            Toast.makeText(this, "Data penghuni tidak ditemukan. Hubungi admin.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupDatePicker()
        setupMetodeSpinner()

        binding.btnPilihFile.setOnClickListener {
            pilihFile()
        }

        binding.btnHapus.setOnClickListener {
            selectedFile = null
            binding.tvNamaFile.text = "Belum ada file dipilih"
            binding.btnHapus.visibility = View.GONE
        }

        binding.btnKirim.setOnClickListener {
            kirimBukti()
        }
    }

    private fun setupDatePicker() {
        binding.etBulanTahun.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format menjadi "dd MMMM yyyy" (contoh: 01 Juli 2026)
                val date = Calendar.getInstance()
                date.set(selectedYear, selectedMonth, selectedDay)
                val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                binding.etBulanTahun.setText(format.format(date.time))

                // Simpan juga dalam format YYYY-MM untuk database
                val dbFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val bulanTahunDb = dbFormat.format(date.time)
                Log.d(TAG, "Tanggal dipilih: ${format.format(date.time)}, DB format: $bulanTahunDb")
            },
            year,
            month,
            day
        ).show()
    }

    private fun setupMetodeSpinner() {
        val metodeList = listOf("Transfer Bank", "Tunai", "QRIS", "Virtual Account", "E-Wallet")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, metodeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMetode.adapter = adapter
    }

    private fun pilihFile() {
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
                if (path != null) File(path) else copyUriToCache(uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFileFromUri error", e)
            copyUriToCache(uri)
        }
    }

    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "bukti_${timestamp}.jpg"
            val cacheFile = File(cacheDir, fileName)
            val outputStream = FileOutputStream(cacheFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            cacheFile
        } catch (e: Exception) {
            Log.e(TAG, "copyUriToCache error", e)
            null
        }
    }

    private fun kirimBukti() {
        val bulanTahunDisplay = binding.etBulanTahun.text.toString().trim()
        val jumlahStr = binding.etJumlah.text.toString().trim()
        val metode = binding.spinnerMetode.selectedItem.toString()

        if (bulanTahunDisplay.isEmpty() || jumlahStr.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlah = jumlahStr.replace(Regex("[^0-9]"), "").toIntOrNull()
        if (jumlah == null || jumlah <= 0) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedFile == null) {
            Toast.makeText(this, "Pilih file bukti pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi "dd MMMM yyyy" ke "YYYY-MM" untuk database
        val bulanTahunFormat = try {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = sdf.parse(bulanTahunDisplay)
            val outputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            Log.e(TAG, "Format bulan error", e)
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        }

        Log.d(TAG, "Bulan tahun display: $bulanTahunDisplay -> DB format: $bulanTahunFormat")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnKirim.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = repository.tambahPembayaranUser(
                    penghuni_Id = penghuniId,
                    bulanTahun = bulanTahunFormat,
                    jumlah = jumlah,
                    metode = metode,
                    file = selectedFile!!
                )
                binding.progressBar.visibility = View.GONE
                binding.btnKirim.isEnabled = true
                if (success) {
                    Toast.makeText(this@UploadBuktiPembayaranActivity, "Bukti terkirim, menunggu verifikasi", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@UploadBuktiPembayaranActivity, "Gagal upload bukti", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnKirim.isEnabled = true
                Log.e(TAG, "kirimBukti error", e)
                Toast.makeText(this@UploadBuktiPembayaranActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}