package com.example.aplikasi_asrama.keluhan

import android.content.Context
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
import com.bumptech.glide.Glide
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.RetrofitClient.BASE_URL_IMAGE
import com.example.aplikasi_asrama.databinding.ActivityKeluhanDetailBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File

class KeluhanDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeluhanDetailBinding
    private val repository = ApiRepository()
    private var keluhanId: Int = 0
    private var selectedPhotoFile: File? = null
    private val statusItems = arrayOf("menunggu", "proses", "selesai")
    private val TAG = "KeluhanDetail"

    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val role by lazy { prefs.getString(LoginActivity.KEY_ROLE, "") ?: "" }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    companion object {
        fun start(context: Context, id: Int) {
            val intent = Intent(context, KeluhanDetailActivity::class.java)
            intent.putExtra("KELUHAN_ID", id)
            context.startActivity(intent)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedPhotoFile = getFileFromUri(uri)
                binding.tvNamaFotoPerbaikan.text = selectedPhotoFile?.name ?: "Foto dipilih"
                binding.ivPreviewFotoPerbaikan.setImageURI(uri)
                binding.ivPreviewFotoPerbaikan.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeluhanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Keluhan"

        keluhanId = intent.getIntExtra("KELUHAN_ID", 0)
        if (keluhanId == 0) {
            Toast.makeText(this, "ID keluhan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        loadData()

        binding.btnPilihFotoPerbaikan.setOnClickListener { pilihFotoPerbaikan() }
        binding.btnHapusFotoPerbaikan.setOnClickListener {
            selectedPhotoFile = null
            binding.tvNamaFotoPerbaikan.text = "Belum ada foto dipilih"
            binding.ivPreviewFotoPerbaikan.visibility = View.GONE
            binding.ivPreviewFotoPerbaikan.setImageURI(null)
        }
        binding.btnSimpan.setOnClickListener { simpanTanggapan() }
        binding.btnSelesai.setOnClickListener { selesaikanKeluhan() }
    }

    private fun pilihFotoPerbaikan() {
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

    private fun loadData() {
        if (isFinishing || isDestroyed) return

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getKeluhanById(keluhanId)
                if (isFinishing || isDestroyed) return@launch
                binding.progressBar.visibility = View.GONE

                if (data != null) {
                    binding.tvNama.text = "Nama: ${data.namaPenghuni}"
                    binding.tvKamar.text = "Kamar: ${data.nomorKamar}"
                    binding.tvJudul.text = data.judulKeluhan
                    binding.tvDeskripsi.text = data.deskripsiKeluhan
                    binding.tvTanggal.text = "Tanggal: ${data.tanggalKeluhan}"

                    val statusText = when (data.status) {
                        "menunggu" -> "Menunggu"
                        "proses" -> "Diproses"
                        "selesai" -> "Selesai"
                        else -> "Menunggu"
                    }
                    binding.tvStatus.text = statusText

                    when (data.status) {
                        "menunggu" -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_keluhan_menunggu)
                        "proses" -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_keluhan_proses)
                        "selesai" -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_keluhan_selesai)
                        else -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_keluhan_menunggu)
                    }

                    // Foto Keluhan
                    if (!data.fotoKeluhanUri.isNullOrEmpty()) {
                        val fotoUrl = BASE_URL_IMAGE + data.fotoKeluhanUri
                        Glide.with(this@KeluhanDetailActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_error)
                            .into(binding.ivFotoKeluhan)
                        binding.ivFotoKeluhan.visibility = View.VISIBLE
                    } else {
                        binding.ivFotoKeluhan.visibility = View.GONE
                    }

                    // Foto Perbaikan
                    if (!data.fotoPerbaikanUri.isNullOrEmpty()) {
                        val fotoUrl = BASE_URL_IMAGE + data.fotoPerbaikanUri
                        Glide.with(this@KeluhanDetailActivity)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_error)
                            .into(binding.ivFotoPerbaikan)
                        binding.ivFotoPerbaikan.visibility = View.VISIBLE
                    } else {
                        binding.ivFotoPerbaikan.visibility = View.GONE
                    }

                    val position = statusItems.indexOf(data.status)
                    if (position >= 0) binding.spinnerStatus.setSelection(position)

                    if (!data.tanggapanAdmin.isNullOrEmpty()) {
                        binding.cardTanggapan.visibility = View.VISIBLE
                        binding.tvTanggapan.text = data.tanggapanAdmin
                        binding.tvTanggalPerbaikan.text = "Tanggal: ${data.tanggalPerbaikan ?: "Belum diperbaiki"}"
                    } else {
                        binding.cardTanggapan.visibility = View.GONE
                    }

                    if (data.status == "selesai" || role != "admin") {
                        binding.cardAdminAction.visibility = View.GONE
                        binding.btnSelesai.isEnabled = false
                    } else {
                        binding.cardAdminAction.visibility = View.VISIBLE
                        binding.btnSelesai.isEnabled = true
                    }
                } else {
                    Toast.makeText(this@KeluhanDetailActivity, "Data keluhan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "loadData error", e)
                Toast.makeText(this@KeluhanDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simpanTanggapan() {
        val tanggapan = binding.etTanggapan.text.toString().trim()
        if (tanggapan.isEmpty()) {
            Toast.makeText(this, "Tanggapan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val status = binding.spinnerStatus.selectedItem.toString()

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val success = repository.tanggapiKeluhan(
                    id = keluhanId,
                    tanggapan = tanggapan,
                    status = status,
                    fotoPerbaikanFile = selectedPhotoFile
                )
                binding.progressBar.visibility = View.GONE
                if (success) {
                    // ===== NOTIFIKASI =====
                    val data = repository.getKeluhanById(keluhanId)
                    if (data != null && data.userId > 0) {
                        if (status == "proses") {
                            NotifikasiHelper.keluhanDiproses(
                                this@KeluhanDetailActivity,
                                data.userId,
                                data.judulKeluhan,
                                data.id
                            )
                        } else if (status == "selesai") {
                            NotifikasiHelper.keluhanSelesai(
                                this@KeluhanDetailActivity,
                                data.userId,
                                data.judulKeluhan,
                                tanggapan,
                                data.id
                            )
                        }
                    }
                    Toast.makeText(this@KeluhanDetailActivity, "Tanggapan berhasil dikirim", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@KeluhanDetailActivity, "Gagal mengirim tanggapan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "simpanTanggapan error", e)
                Toast.makeText(this@KeluhanDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selesaikanKeluhan() {
        lifecycleScope.launch {
            try {
                val success = repository.updateStatusKeluhan(keluhanId, "selesai")
                if (success) {
                    val data = repository.getKeluhanById(keluhanId)
                    if (data != null && data.userId > 0) {
                        NotifikasiHelper.keluhanSelesai(
                            this@KeluhanDetailActivity,
                            data.userId,
                            data.judulKeluhan,
                            data.tanggapanAdmin ?: "Keluhan selesai",
                            data.id
                        )
                    }
                    Toast.makeText(this@KeluhanDetailActivity, "Keluhan selesai", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@KeluhanDetailActivity, "Gagal mengupdate status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "selesaikanKeluhan error", e)
                Toast.makeText(this@KeluhanDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}