package com.example.aplikasi_asrama.keluhan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityKeluhanUserBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeluhanUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeluhanUserBinding
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val penghuniId by lazy { prefs.getInt(LoginActivity.KEY_PENGHUNI_ID, 0) }
    private val TAG = "KeluhanUser"

    private var selectedPhotoFile: File? = null
    private var selectedPhotoUri: Uri? = null

    // Launcher untuk memilih foto dari galeri
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                selectedPhotoUri = uri
                selectedPhotoFile = getFileFromUri(uri)
                binding.tvNamaFoto.text = selectedPhotoFile?.name ?: "Foto dipilih"
                Log.d(TAG, "Foto dipilih: ${selectedPhotoFile?.absolutePath}")
            } else {
                Toast.makeText(this, "Gagal memilih foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeluhanUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kirim Keluhan"

        loadUserData()

        binding.btnPilihFoto.setOnClickListener {
            pilihFoto()
        }

        binding.btnHapusFoto.setOnClickListener {
            selectedPhotoFile = null
            selectedPhotoUri = null
            binding.tvNamaFoto.text = "Belum ada foto dipilih"
        }

        binding.btnKirim.setOnClickListener {
            kirimKeluhan()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val penghuni = repository.getPenghuniById(penghuniId)
                if (penghuni != null) {
                    binding.etnamaPenghuni.setText(penghuni.nama)
                    binding.etnomorKamar.setText(penghuni.nomorKamar ?: "-")
                } else {
                    Toast.makeText(this@KeluhanUserActivity, "Data penghuni tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadUserData error", e)
            }
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

    private fun kirimKeluhan() {
        val nama = binding.etnamaPenghuni.text.toString().trim()
        val kamar = binding.etnomorKamar.text.toString().trim()
        val judul = binding.etJudulKeluhan.text.toString().trim()
        val deskripsi = binding.etDeskripsiKeluhan.text.toString().trim()

        if (nama.isEmpty() || kamar.isEmpty() || judul.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPhotoFile == null) {
            Toast.makeText(this, "Foto bukti kerusakan wajib dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        binding.progressBar.visibility = View.VISIBLE
        binding.btnKirim.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = repository.tambahKeluhan(
                    userId = userId,
                    namaPenghuni = nama,
                    nomorKamar = kamar,
                    judulKeluhan = judul,
                    deskripsiKeluhan = deskripsi,
                    tanggalKeluhan = tanggal,
                    fotoFile = selectedPhotoFile
                )
                binding.progressBar.visibility = View.GONE
                binding.btnKirim.isEnabled = true

                if (success) {
                    Toast.makeText(this@KeluhanUserActivity, "Keluhan berhasil dikirim", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@KeluhanUserActivity, "Gagal mengirim keluhan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnKirim.isEnabled = true
                Log.e(TAG, "kirimKeluhan error", e)
                Toast.makeText(this@KeluhanUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}