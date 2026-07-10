package com.example.aplikasi_asrama.paket

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.api.RetrofitClient.BASE_URL_IMAGE
import com.example.aplikasi_asrama.databinding.ActivityDetailPaketBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class DetailPaketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPaketBinding
    private val repository = ApiRepository()
    private var paketId: Int = 0
    private var fromUser: Boolean = false
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val role by lazy { prefs.getString(LoginActivity.KEY_ROLE, "") ?: "" }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPaketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Paket"

        paketId = intent.getIntExtra("PAKET_ID", 0)
        fromUser = intent.getBooleanExtra("FROM_USER", false)

        if (paketId == 0) {
            Toast.makeText(this, "ID paket tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadData()
        setupButtons()
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getPaketById(paketId)
                binding.progressBar.visibility = View.GONE
                if (data != null) {
                    binding.tvNamaPenghuni.text = "Penghuni: ${data.namaPenghuni}"
                    binding.tvKamar.text = "Kamar: ${data.nomorKamar}"
                    binding.tvPengirim.text = "Pengirim: ${data.namaPengirim}"
                    binding.tvJenisPaket.text = "Jenis: ${data.jenisPaket}"
                    binding.tvTanggal.text = "Tanggal: ${data.tanggalDatang}"

                    val statusText = when (data.status) {
                        "menunggu" -> "Menunggu"
                        "diambil" -> "Diambil"
                        "selesai" -> "Selesai"
                        else -> "Menunggu"
                    }
                    binding.tvStatus.text = statusText

                    val statusColor = when (data.status) {
                        "menunggu" -> R.drawable.bg_status_paket_menunggu
                        "diambil" -> R.drawable.bg_status_paket_diambil
                        "selesai" -> R.drawable.bg_status_paket_selesai
                        else -> R.drawable.bg_status_paket_menunggu
                    }
                    binding.tvStatus.setBackgroundResource(statusColor)

                    if (!data.fotoUri.isNullOrEmpty()) {
                        binding.ivFoto.visibility = View.VISIBLE
                        val imageUrl = BASE_URL_IMAGE + data.fotoUri
                        Glide.with(this@DetailPaketActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .into(binding.ivFoto)
                    } else {
                        binding.ivFoto.visibility = View.GONE
                    }

                    if (fromUser || role == "user") {
                        binding.cardActions.visibility = View.GONE
                    } else {
                        binding.cardActions.visibility = View.VISIBLE
                        when (data.status) {
                            "menunggu" -> {
                                binding.btnTerima.visibility = View.VISIBLE
                                binding.btnTolak.visibility = View.VISIBLE
                                binding.btnSelesai.visibility = View.GONE
                            }
                            "diambil" -> {
                                binding.btnTerima.visibility = View.GONE
                                binding.btnTolak.visibility = View.GONE
                                binding.btnSelesai.visibility = View.VISIBLE
                            }
                            "selesai" -> {
                                binding.btnTerima.visibility = View.GONE
                                binding.btnTolak.visibility = View.GONE
                                binding.btnSelesai.visibility = View.GONE
                            }
                        }
                        binding.btnHapus.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@DetailPaketActivity, "Paket tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DetailPaketActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.btnTerima.setOnClickListener { updateStatus("diambil") }
        binding.btnTolak.setOnClickListener { updateStatus("selesai") }
        binding.btnSelesai.setOnClickListener { updateStatus("selesai") }
        binding.btnHapus.setOnClickListener { hapusPaket() }
    }

    private fun updateStatus(status: String) {
        lifecycleScope.launch {
            try {
                val success = repository.updateStatusPaket(paketId, status)
                if (success) {
                    // ===== NOTIFIKASI =====
                    val data = repository.getPaketById(paketId)
                    if (data != null && data.userid > 0) {
                        NotifikasiHelper.paketBaru(
                            this@DetailPaketActivity,
                            data.userid,
                            data.namaPenghuni,
                            data.jenisPaket,
                            data.namaPengirim,
                            data.id
                        )
                    }
                    Toast.makeText(this@DetailPaketActivity, "Status berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@DetailPaketActivity, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailPaketActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusPaket() {
        lifecycleScope.launch {
            try {
                val success = repository.hapusPaket(paketId)
                if (success) {
                    Toast.makeText(this@DetailPaketActivity, "Paket berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetailPaketActivity, "Gagal hapus paket", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailPaketActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}