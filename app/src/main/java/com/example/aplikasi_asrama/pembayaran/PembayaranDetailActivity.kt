package com.example.aplikasi_asrama.pembayaran

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.api.RetrofitClient
import com.example.aplikasi_asrama.databinding.ActivityPembayaranDetailBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.viewmodel.PembayaranViewModel

class PembayaranDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranDetailBinding
    private lateinit var viewModel: PembayaranViewModel

    private var previousStatus: String? = null
    private var isFirstLoad = true
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val role by lazy { prefs.getString(LoginActivity.KEY_ROLE, "") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pembayaran"

        val pembayaranId = intent.getIntExtra("ID", -1)
        if (pembayaranId == -1) {
            Toast.makeText(this, "ID pembayaran tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(PembayaranViewModel::class.java)
        observeData()
        viewModel.loadPembayaranById(pembayaranId)
    }

    private fun observeData() {
        viewModel.pembayaranDetail.observe(this) { pembayaran ->
            if (pembayaran != null) {
                val currentStatus = pembayaran.status
                val statusChanged = previousStatus != null && previousStatus != currentStatus

                binding.tvNamaPenghuni.text = pembayaran.namaPenghuni ?: "-"
                binding.tvNomorKamar.text = "Kamar ${pembayaran.nomorKamar ?: "-"}"
                binding.tvBulanTahun.text = pembayaran.bulanTahun
                binding.tvJumlah.text = "Rp ${"%,d".format(pembayaran.jumlah)}"
                binding.tvTanggalTagihan.text = pembayaran.tanggalBayar ?: "-"
                binding.tvStatus.text = pembayaran.status
                binding.tvTanggalBayar.text = pembayaran.tanggalBayar ?: "-"
                binding.tvMetode.text = pembayaran.metode ?: "-"
                binding.tvCatatan.text = pembayaran.catatanAdmin ?: "Tidak ada catatan"

                when (pembayaran.status) {
                    "Lunas" -> binding.tvStatus.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                    "Ditolak" -> binding.tvStatus.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    else -> binding.tvStatus.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                }

                if (!pembayaran.buktiUrl.isNullOrBlank()) {
                    binding.ivBukti.visibility = android.view.View.VISIBLE
                    val imageUrl = RetrofitClient.BASE_URL_IMAGE + pembayaran.buktiUrl
                    binding.ivBukti.load(imageUrl) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_report_image)
                    }
                } else {
                    binding.ivBukti.visibility = android.view.View.GONE
                }

                if (!isFirstLoad && statusChanged && role != "admin" && userId > 0) {
                    when (currentStatus) {
                        "Lunas" -> {
                            NotifikasiHelper.pembayaranDiverifikasi(
                                context = this@PembayaranDetailActivity,
                                userId = userId,
                                penghuni = pembayaran.namaPenghuni ?: "Penghuni",
                                jumlah = pembayaran.jumlah,
                                pembayaranId = pembayaran.id
                            )
                        }
                        "Ditolak" -> {
                            NotifikasiHelper.pembayaranDitolak(
                                context = this@PembayaranDetailActivity,
                                userId = userId,
                                penghuni = pembayaran.namaPenghuni ?: "Penghuni",
                                jumlah = pembayaran.jumlah,
                                pembayaranId = pembayaran.id
                            )
                        }
                    }
                }

                previousStatus = currentStatus
                isFirstLoad = false

            } else {
                Toast.makeText(this, "Data pembayaran tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.message.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}