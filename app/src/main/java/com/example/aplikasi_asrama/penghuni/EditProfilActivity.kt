package com.example.aplikasi_asrama.penghuni

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
import com.bumptech.glide.Glide
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.RetrofitClient.BASE_URL_IMAGE
import com.example.aplikasi_asrama.databinding.ActivityEditProfilBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfilBinding
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val penghuniId by lazy { prefs.getInt(LoginActivity.KEY_PENGHUNI_ID, 0) }
    private val TAG = "EditProfil"

    private var selectedPhotoFile: File? = null
    private var currentPhotoUrl: String? = null

    // Launcher untuk memilih foto dari galeri
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedPhotoFile = getFileFromUri(uri)
                if (selectedPhotoFile != null) {
                    binding.ivFoto.setImageURI(uri)
                    binding.tvNamaFoto.text = selectedPhotoFile?.name ?: "Foto dipilih"
                    binding.btnHapusFoto.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Gagal memilih foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profil"

        loadData()

        // ===== KLIK FOTO UNTUK PILIH =====
        binding.ivFoto.setOnClickListener {
            pilihFoto()
        }

        binding.btnPilihFoto.setOnClickListener {
            pilihFoto()
        }

        binding.btnHapusFoto.setOnClickListener {
            selectedPhotoFile = null
            binding.ivFoto.setImageResource(R.drawable.ic_default_avatar)
            binding.tvNamaFoto.text = "Belum ada foto"
            binding.btnHapusFoto.visibility = View.GONE
        }

        binding.btnSimpan.setOnClickListener {
            simpanProfil()
        }
    }

    private fun loadData() {
        if (penghuniId == 0) {
            Toast.makeText(this, "Data penghuni tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val penghuni = repository.getPenghuniById(penghuniId)
                binding.progressBar.visibility = View.GONE
                if (penghuni != null) {
                    binding.etNama.setText(penghuni.nama)
                    binding.etNim.setText(penghuni.Nim ?: "")
                    binding.etNoTelp.setText(penghuni.noTelp ?: "")
                    binding.etAlamat.setText(penghuni.alamatAsal ?: "")

                    // Load foto
                    currentPhotoUrl = penghuni.fotoUri
                    if (!penghuni.fotoUri.isNullOrEmpty()) {
                        val imageUrl = BASE_URL_IMAGE + penghuni.fotoUri
                        Glide.with(this@EditProfilActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.ivFoto)
                    } else {
                        binding.ivFoto.setImageResource(R.drawable.ic_default_avatar)
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Load data error", e)
                Toast.makeText(this@EditProfilActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
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
            val fileName = "profil_$timestamp.jpg"
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

    private fun simpanProfil() {
        val nama = binding.etNama.text.toString().trim()
        val nim = binding.etNim.text.toString().trim()
        val noTelp = binding.etNoTelp.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. Update data profil (tanpa foto)
                val penghuniLama = repository.getPenghuniById(penghuniId)
                val penghuni = penghuniLama?.copy(
                    nama = nama,
                    Nim = nim.ifEmpty { penghuniLama.Nim },
                    noTelp = noTelp.ifEmpty { penghuniLama.noTelp },
                    alamatAsal = alamat.ifEmpty { penghuniLama.alamatAsal }
                )

                var success = true
                if (penghuni != null) {
                    success = repository.editPenghuniJSON(penghuni)
                }

                // 2. Jika ada foto baru, upload
                var fotoSuccess = true
                if (success && selectedPhotoFile != null) {
                    fotoSuccess = repository.updateFotoPenghuni(penghuniId, selectedPhotoFile)
                    if (!fotoSuccess) {
                        Toast.makeText(this@EditProfilActivity, "Data berhasil diupdate, tapi gagal upload foto", Toast.LENGTH_LONG).show()
                    }
                }

                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true

                if (success && (selectedPhotoFile == null || fotoSuccess)) {
                    Toast.makeText(this@EditProfilActivity, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (!success) {
                    Toast.makeText(this@EditProfilActivity, "Gagal update profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Log.e(TAG, "Simpan error", e)
                Toast.makeText(this@EditProfilActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}