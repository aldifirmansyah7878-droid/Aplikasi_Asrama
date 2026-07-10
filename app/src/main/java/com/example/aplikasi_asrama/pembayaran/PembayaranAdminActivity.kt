package com.example.aplikasi_asrama.pembayaran

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.databinding.ActivityPembayaranAdminBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper  // <-- TAMBAHKAN
import com.example.aplikasi_asrama.viewmodel.PembayaranViewModel

class PembayaranAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranAdminBinding
    private lateinit var viewModel: PembayaranViewModel
    private lateinit var adapter: PembayaranAdminAdapter
    private val prefs by lazy { getSharedPreferences("user_pref", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[PembayaranViewModel::class.java]

        adapter = PembayaranAdminAdapter(
            onVerifikasi = { p -> showVerifikasiDialog(p) },
            onTolak = { p -> showTolakDialog(p) }
        )
        binding.rvPembayaran.adapter = adapter
        binding.rvPembayaran.layoutManager = LinearLayoutManager(this)

        // Filter buttons
        binding.btnSemua.setOnClickListener { viewModel.loadPembayaranAdmin(null) }
        binding.btnMenunggu.setOnClickListener { viewModel.loadPembayaranAdmin("Menunggu") }
        binding.btnLunas.setOnClickListener { viewModel.loadPembayaranAdmin("Lunas") }
        binding.btnDitolak.setOnClickListener { viewModel.loadPembayaranAdmin("Ditolak") }

        // FAB Tambah
        binding.fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahPembayaranActivity::class.java))
        }

        observeData()
        viewModel.loadPembayaranAdmin(null)
    }

    private fun observeData() {
        viewModel.pembayaranAdmin.observe(this) { list ->
            adapter.submitList(list ?: emptyList())
        }
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        viewModel.message.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showVerifikasiDialog(p: PembayaranData) {
        val admin = getSharedPreferences("user_pref", MODE_PRIVATE).getString("username", "Admin") ?: "Admin"
        AlertDialog.Builder(this)
            .setTitle("Verifikasi")
            .setMessage("Setujui pembayaran ${p.bulanTahun}?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.verifikasiPembayaran(p.id, "Lunas", "", admin) { success ->
                    if (success) {
                        viewModel.loadPembayaranAdmin(null)
                        // ===== KIRIM NOTIFIKASI =====
                        val userId = prefs.getInt("userId", 0)
                        NotifikasiHelper.pembayaranDiverifikasi(
                            this@PembayaranAdminActivity,
                            userId,
                            p.namaPenghuni ?: "Penghuni",
                            p.jumlah,
                            p.id
                        )
                    } else {
                        Toast.makeText(this, "Gagal verifikasi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showTolakDialog(p: PembayaranData) {
        val input = android.widget.EditText(this).apply { hint = "Alasan penolakan" }
        val admin = getSharedPreferences("user_pref", MODE_PRIVATE).getString("username", "Admin") ?: "Admin"
        AlertDialog.Builder(this)
            .setTitle("Tolak")
            .setView(input)
            .setPositiveButton("Tolak") { _, _ ->
                val catatan = input.text.toString().trim()
                if (catatan.isEmpty()) { Toast.makeText(this, "Alasan wajib", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                viewModel.verifikasiPembayaran(p.id, "Ditolak", catatan, admin) { success ->
                    if (success) {
                        viewModel.loadPembayaranAdmin(null)
                        // ===== KIRIM NOTIFIKASI =====
                        val userId = prefs.getInt("userId", 0)
                        NotifikasiHelper.pembayaranDitolak(
                            this@PembayaranAdminActivity,
                            userId,
                            p.namaPenghuni ?: "Penghuni",
                            p.jumlah,
                            p.id
                        )
                    } else {
                        Toast.makeText(this, "Gagal tolak", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}