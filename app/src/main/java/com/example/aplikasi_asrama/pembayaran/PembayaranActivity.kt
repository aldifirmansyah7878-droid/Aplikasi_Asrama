package com.example.aplikasi_asrama.pembayaran

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.aplikasi_asrama.databinding.ActivityPembayaranDetailBinding
import com.example.aplikasi_asrama.viewmodel.PembayaranViewModel
import java.io.File

class PembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranDetailBinding
    private lateinit var viewModel: PembayaranViewModel

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
                binding.tvNamaPenghuni.text = pembayaran.namaPenghuni
                binding.tvNomorKamar.text = "Kamar ${pembayaran.nomorKamar}"
                binding.tvBulanTahun.text = pembayaran.bulanTahun
                binding.tvJumlah.text = "Rp ${"%,d".format(pembayaran.jumlah)}"
                binding.tvTanggalTagihan.text = pembayaran.tanggalBayar   // gunakan tanggalBayar
                binding.tvStatus.text = pembayaran.status
                binding.tvTanggalBayar.text = pembayaran.tanggalBayar

                if (!pembayaran.buktiUrl.isNullOrBlank()) {
                    binding.ivBukti.visibility = android.view.View.VISIBLE
                    binding.ivBukti.load(File(pembayaran.buktiUrl))
                } else {
                    binding.ivBukti.visibility = android.view.View.GONE
                }
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