package com.example.aplikasi_asrama.izin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.MyApp
import com.example.aplikasi_asrama.R
import com.example.aplikasi_asrama.databinding.ActivityDetailIzinBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.ui.izin.IzinViewModelFactory
import kotlinx.coroutines.launch

class DetailIzinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailIzinBinding
    private lateinit var viewModel: IzinViewModel
    private var izinId: Int = -1
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val role by lazy { prefs.getString(LoginActivity.KEY_ROLE, "") ?: "" }
    private val repository = ApiRepository()
    private var currentIzin: com.example.aplikasi_asrama.api.model.IzinData? = null
    private var isFinishingFlag = false

    companion object {
        fun start(context: Context, id: Int) {
            val intent = Intent(context, DetailIzinActivity::class.java)
            intent.putExtra("IZIN_ID", id)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailIzinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        izinId = intent.getIntExtra("IZIN_ID", -1)
        if (izinId == -1) {
            Toast.makeText(this, "ID izin tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViewModel()
        observeDetail()
        setupListeners()

        viewModel.fetchIzinDetail(izinId)

        if (role != "admin") {
            binding.btnKembali.visibility = View.GONE
            binding.linearAdminActions.visibility = View.GONE
        } else {
            binding.linearAdminActions.visibility = View.VISIBLE
        }
    }

    private fun setupViewModel() {
        val repository = (application as MyApp).apiRepository
        val factory = IzinViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(IzinViewModel::class.java)
    }

    private fun observeDetail() {
        viewModel.detailState.observe(this) { state ->
            if (isFinishing || isDestroyed || isFinishingFlag) return@observe

            when (state) {
                is IzinUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is IzinUiState.DetailSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    bindData(state.data)
                }
                is IzinUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.operationResult.observe(this) { result ->
            if (isFinishing || isDestroyed || isFinishingFlag) return@observe

            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    if (!isFinishing && !isDestroyed && !isFinishingFlag) {
                        isFinishingFlag = true
                        finish()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun bindData(izin: com.example.aplikasi_asrama.api.model.IzinData) {
        currentIzin = izin

        binding.tvNama.text = "Nama: ${izin.namaPenghuni}"
        binding.tvKamar.text = "Kamar: ${izin.nomorKamar}"
        binding.tvKeperluan.text = "Keperluan: ${izin.keperluan}"
        binding.tvTanggalKeluar.text = "Keluar: ${izin.tanggalKeluar} ${izin.jamKeluar}"
        binding.tvPerkiraanKembali.text = "Perkiraan Kembali: ${izin.perkiraanKembali}"

        // ===== STATUS & WARNA =====
        val statusText = when (izin.status) {
            "keluar" -> "Keluar"
            "kembali" -> "Kembali"
            "disetujui" -> "Disetujui"
            "ditolak" -> "Ditolak"
            else -> "Keluar"
        }
        binding.tvStatus.text = "Status: $statusText"

        val statusColor = when (izin.status) {
            "keluar" -> R.drawable.bg_status_keluar
            "kembali" -> R.drawable.bg_status_kembali
            "disetujui" -> R.drawable.bg_status_disetujui
            "ditolak" -> R.drawable.bg_status_ditolak
            else -> R.drawable.bg_status_keluar
        }
        binding.tvStatus.setBackgroundResource(statusColor)

        binding.tvLetakKunci.text = "Letak Kunci: ${izin.letakKunci}"
        binding.tvCatatanKunci.text = "Catatan: ${izin.catatanKunci ?: "-"}"
        binding.tvAlamat.text = "Alamat: ${izin.alamatLokasi}"
        binding.tvCreated.text = "Dibuat: ${izin.createdAt}"

        // ===== ADMIN ACTIONS =====
        if (role == "admin") {
            when (izin.status) {
                "keluar" -> {
                    binding.linearAdminActions.visibility = View.VISIBLE
                    binding.btnKembali.visibility = View.GONE
                    binding.btnHapus.visibility = View.VISIBLE
                }
                "disetujui" -> {
                    binding.linearAdminActions.visibility = View.GONE
                    binding.btnKembali.visibility = View.VISIBLE
                    binding.btnHapus.visibility = View.VISIBLE
                }
                "ditolak", "kembali" -> {
                    binding.linearAdminActions.visibility = View.GONE
                    binding.btnKembali.visibility = View.GONE
                    binding.btnHapus.visibility = View.VISIBLE
                }
                else -> {
                    binding.linearAdminActions.visibility = View.GONE
                    binding.btnKembali.visibility = View.GONE
                    binding.btnHapus.visibility = View.VISIBLE
                }
            }
        } else {
            binding.linearAdminActions.visibility = View.GONE
            binding.btnKembali.visibility = View.GONE
            binding.btnHapus.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnKembali.setOnClickListener {
            if (!isFinishing && !isDestroyed && !isFinishingFlag) {
                viewModel.returnIzin(izinId)
            }
        }

        binding.btnSetujui.setOnClickListener {
            if (!isFinishing && !isDestroyed && !isFinishingFlag) {
                updateStatus("disetujui")
            }
        }

        binding.btnTolak.setOnClickListener {
            if (!isFinishing && !isDestroyed && !isFinishingFlag) {
                updateStatus("ditolak")
            }
        }

        binding.btnHapus.setOnClickListener {
            if (!isFinishing && !isDestroyed && !isFinishingFlag) {
                viewModel.deleteIzin(izinId)
            }
        }
    }

    private fun updateStatus(status: String) {
        if (currentIzin == null) return

        lifecycleScope.launch {
            try {
                val success = repository.updateStatusIzin(izinId, status)
                if (success) {
                    Toast.makeText(this@DetailIzinActivity, "Status berhasil diupdate", Toast.LENGTH_SHORT).show()

                    currentIzin?.let { izin ->
                        when (status) {
                            "disetujui" -> {
                                NotifikasiHelper.izinDisetujui(
                                    this@DetailIzinActivity,
                                    izin.userId,
                                    izin.namaPenghuni,
                                    izin.id
                                )
                            }
                            "ditolak" -> {
                                NotifikasiHelper.izinDitolak(
                                    this@DetailIzinActivity,
                                    izin.userId,
                                    izin.namaPenghuni,
                                    izin.id
                                )
                            }
                        }
                    }

                    viewModel.fetchIzinDetail(izinId)
                } else {
                    Toast.makeText(this@DetailIzinActivity, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailIzinActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isFinishingFlag = true
        viewModel.detailState.removeObservers(this)
        viewModel.operationResult.removeObservers(this)
    }
}