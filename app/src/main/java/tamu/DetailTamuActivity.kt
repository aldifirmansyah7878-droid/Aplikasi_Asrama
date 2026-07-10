package com.example.aplikasi_asrama.tamu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.databinding.ActivityDetailTamuBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class DetailTamuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTamuBinding
    private val repository = ApiRepository()
    private var tamuId: Int = 0
    private var fromUser: Boolean = false
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val role by lazy { prefs.getString(LoginActivity.KEY_ROLE, "") ?: "" }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTamuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Tamu"

        tamuId = intent.getIntExtra("TAMU_ID", 0)
        fromUser = intent.getBooleanExtra("FROM_USER", false)

        if (tamuId == 0) {
            Toast.makeText(this, "ID tamu tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (fromUser || role == "user") {
            binding.btnSetuju.visibility = View.GONE
            binding.btnTolak.visibility = View.GONE
            binding.btnSelesai.visibility = View.GONE
            binding.btnHapus.visibility = View.GONE
        } else {
            binding.btnSetuju.visibility = View.VISIBLE
            binding.btnTolak.visibility = View.VISIBLE
            binding.btnSelesai.visibility = View.VISIBLE
            binding.btnHapus.visibility = View.VISIBLE
        }

        loadData()
        setupButtons()
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val data = repository.getTamuById(tamuId)
                binding.progressBar.visibility = View.GONE
                if (data != null) {
                    binding.tvNamaTamu.text = "Nama: ${data.namaTamu}"
                    binding.tvTujuan.text = "Tujuan: ${data.tujuanPenghuni}"
                    binding.tvKamar.text = "Kamar: ${data.nomorKamar}"
                    binding.tvWaktu.text = "Waktu: ${data.waktuKunjungan}"
                    binding.tvHubungan.text = "Hubungan: ${data.hubungan}"

                    val statusText = when (data.status) {
                        "menunggu" -> "Menunggu"
                        "disetujui" -> "Disetujui"
                        "ditolak" -> "Ditolak"
                        "selesai" -> "Selesai"
                        else -> "Menunggu"
                    }
                    binding.tvStatus.text = "Status: $statusText"

                    val statusColor = when (data.status) {
                        "menunggu" -> R.drawable.bg_status_tamu_menunggu
                        "disetujui" -> R.drawable.bg_status_tamu_disetujui
                        "ditolak" -> R.drawable.bg_status_tamu_ditolak
                        "selesai" -> R.drawable.bg_status_tamu_selesai
                        else -> R.drawable.bg_status_tamu_menunggu
                    }
                    binding.tvStatus.setBackgroundResource(statusColor)

                    when (data.status) {
                        "menunggu" -> {
                            binding.btnSetuju.visibility = View.VISIBLE
                            binding.btnTolak.visibility = View.VISIBLE
                            binding.btnSelesai.visibility = View.GONE
                        }
                        "disetujui" -> {
                            binding.btnSetuju.visibility = View.GONE
                            binding.btnTolak.visibility = View.GONE
                            binding.btnSelesai.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.btnSetuju.visibility = View.GONE
                            binding.btnTolak.visibility = View.GONE
                            binding.btnSelesai.visibility = View.GONE
                        }
                    }
                } else {
                    Toast.makeText(this@DetailTamuActivity, "Data tamu tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DetailTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.btnSetuju.setOnClickListener { updateStatus("disetujui") }
        binding.btnTolak.setOnClickListener { updateStatus("ditolak") }
        binding.btnSelesai.setOnClickListener { updateStatus("selesai") }
        binding.btnHapus.setOnClickListener { hapusTamu() }
    }

    private fun updateStatus(status: String) {
        lifecycleScope.launch {
            try {
                val success = repository.updateStatusTamu(tamuId, status)
                if (success) {
                    val data = repository.getTamuById(tamuId)
                    if (data != null && data.userid > 0) {
                        NotifikasiHelper.tamuDatang(
                            this@DetailTamuActivity,
                            data.userid,
                            data.namaTamu,
                            data.tujuanPenghuni,
                            data.id
                        )
                    }
                    Toast.makeText(this@DetailTamuActivity, "Status berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@DetailTamuActivity, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusTamu() {
        lifecycleScope.launch {
            try {
                val success = repository.hapusTamu(tamuId)
                if (success) {
                    Toast.makeText(this@DetailTamuActivity, "Tamu berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetailTamuActivity, "Gagal hapus tamu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}