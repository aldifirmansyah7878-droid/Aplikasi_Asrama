package com.example.aplikasi_asrama

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.aplikasi_asrama.databinding.ActivityUserDashboardBinding
import com.example.aplikasi_asrama.izin.RiwayatIzinActivity
import com.example.aplikasi_asrama.izin.TambahIzinActivity
import com.example.aplikasi_asrama.keluhan.KeluhanUserActivity
import com.example.aplikasi_asrama.keluhan.RiwayatKeluhanActivity
import com.example.aplikasi_asrama.notifikasi.NotifikasiActivity
import com.example.aplikasi_asrama.paket.PaketUserActivity
import com.example.aplikasi_asrama.pembayaran.PembayaranUserActivity
import com.example.aplikasi_asrama.penghuni.EditProfilActivity
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.tamu.TambahTamuActivity
import kotlinx.coroutines.launch

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val TAG = "UserDashboard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Dashboard Penghuni"

        if (userId == 0) {
            Toast.makeText(this, "Session tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show()
            logout()
            return
        }

        loadDataPenghuni()
        setupClickListeners()

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.ivEditProfil.setOnClickListener {
            startActivity(Intent(this, EditProfilActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_riwayat_izin -> {
                startActivity(Intent(this, RiwayatIzinActivity::class.java))
                true
            }
            R.id.action_riwayat_keluhan -> {
                startActivity(Intent(this, RiwayatKeluhanActivity::class.java))
                true
            }
            R.id.action_notifikasi -> {
                startActivity(Intent(this, NotifikasiActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ========== LOAD DATA ==========
    private fun loadDataPenghuni() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val repo = ApiRepository()
                val penghuni = repo.getPenghuniByUserId(userId)

                binding.progressBar.visibility = View.GONE

                if (penghuni != null) {
                    binding.tvNamaPenghuni.text = penghuni.nama
                    binding.tvNomorKamar.text = penghuni.nomorKamar ?: "-"
                    binding.tvTanggalMasuk.text = penghuni.tanggalMasuk ?: "-"

                    val statusDb = penghuni.status ?: ""
                    Log.d(TAG, "Status dari database: '$statusDb'")
                    binding.tvStatus.text = if (statusDb.lowercase() == "aktif") {
                        "Aktif"
                    } else {
                        "Tidak Aktif"
                    }

                    val initial = if (penghuni.nama.isNotEmpty()) penghuni.nama[0].toString() else "P"
                    binding.tvAvatar.text = initial.uppercase()

                    // Tampilkan foto profil
                    loadFotoProfil(penghuni.fotoUri)

                    binding.tvInfoBayar.text = "Data pembayaran dapat dilihat di menu Pembayaran Sewa"
                    prefs.edit().putInt(LoginActivity.KEY_PENGHUNI_ID, penghuni.id).apply()
                    binding.tvErrorMessage.visibility = View.GONE

                    loadTotalPembayaran(penghuni.id)

                } else {
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = "Data penghuni belum ditambahkan oleh admin. Hubungi admin."
                    binding.tvNamaPenghuni.text = "Data tidak ditemukan"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error load data penghuni", e)
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = "Gagal memuat data: ${e.message}"
            }
        }
    }

    private fun loadFotoProfil(fotoUri: String?) {
        if (!fotoUri.isNullOrEmpty()) {
            val imageUrl = "http://10.0.2.2/aplikasi_asrama/$fotoUri"
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.ivAvatar)
            binding.ivAvatar.visibility = View.VISIBLE
            binding.tvAvatar.visibility = View.GONE
        } else {
            binding.ivAvatar.visibility = View.GONE
            binding.tvAvatar.visibility = View.VISIBLE
        }
    }

    private fun loadTotalPembayaran(penghuniId: Int) {
        lifecycleScope.launch {
            try {
                val repo = ApiRepository()
                val pembayaran = repo.getPembayaranUser(penghuniId)
                val total = pembayaran.sumOf { it.jumlah }
                binding.tvTotalBayar.text = "Rp $total"
            } catch (e: Exception) {
                Log.e(TAG, "Error load total pembayaran", e)
                binding.tvTotalBayar.text = "Rp 0"
            }
        }
    }

    // ========== CLICK LISTENERS ==========
    private fun setupClickListeners() {
        binding.cardPembayaran.setOnClickListener {
            startActivity(Intent(this, PembayaranUserActivity::class.java))
        }

        binding.cardIzin.setOnClickListener {
            startActivity(Intent(this, TambahIzinActivity::class.java))
        }

        binding.cardRiwayatIzin.setOnClickListener {
            startActivity(Intent(this, RiwayatIzinActivity::class.java))
        }

        binding.cardKeluhan.setOnClickListener {
            startActivity(Intent(this, KeluhanUserActivity::class.java))
        }

        binding.cardRiwayatKeluhan.setOnClickListener {
            startActivity(Intent(this, RiwayatKeluhanActivity::class.java))
        }

        binding.cardPaket.setOnClickListener {
            startActivity(Intent(this, PaketUserActivity::class.java))
        }

        // ===== PERBAIKAN: Kunjungan Tamu (form tambah tamu) =====
        binding.cardTamu.setOnClickListener {
            startActivity(Intent(this, TambahTamuActivity::class.java))
        }
    }

    // ========== LOGOUT ==========
    private fun logout() {
        prefs.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadDataPenghuni()
    }
}