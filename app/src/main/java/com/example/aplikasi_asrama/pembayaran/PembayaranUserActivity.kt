package com.example.aplikasi_asrama.pembayaran

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.databinding.ActivityPembayaranUserBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.viewmodel.PembayaranViewModel
import kotlinx.coroutines.launch

class PembayaranUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranUserBinding
    private lateinit var viewModel: PembayaranViewModel
    private lateinit var adapter: PembayaranUserAdapter
    private var penghuniId = 0
    private val TAG = "PembayaranUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tagihan Saya"

        viewModel = ViewModelProvider(this)[PembayaranViewModel::class.java]

        // ========== AMBIL PENGHUNI_ID DENGAN KONSTANTA YANG SAMA ==========
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        penghuniId = prefs.getInt(LoginActivity.KEY_PENGHUNI_ID, 0)
        Log.d(TAG, "penghuni_id dari SharedPreferences: $penghuniId")

        if (penghuniId == 0) {
            // Coba ambil dari user_id sebagai fallback (jika belum ada penghuni_id)
            val userId = prefs.getInt(LoginActivity.KEY_USER_ID, 0)
            Log.d(TAG, "user_id dari session: $userId")
            if (userId > 0) {
                // Coba fetch penghuni_id dari API
                fetchPenghuniIdFromApi(userId)
                return
            }
            Toast.makeText(this, "Data penghuni tidak ditemukan. Hubungi admin.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        setupSwipeRefresh()
        observeData()

        Log.d(TAG, "Memuat tagihan untuk penghuni_id: $penghuniId")
        viewModel.loadPembayaranUser(penghuniId)

        binding.fabTambah.setOnClickListener {
            startActivity(Intent(this, UploadBuktiPembayaranActivity::class.java))
        }
    }

    private fun fetchPenghuniIdFromApi(userId: Int) {
        lifecycleScope.launch {
            try {
                val repo = ApiRepository()
                val penghuni = repo.getPenghuniByUserId(userId)
                if (penghuni != null) {
                    val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().putInt(LoginActivity.KEY_PENGHUNI_ID, penghuni.id).apply()
                    penghuniId = penghuni.id
                    Log.d(TAG, "penghuni_id berhasil diambil dari API: $penghuniId")
                    // Lanjutkan setup
                    setupRecyclerView()
                    setupSwipeRefresh()
                    observeData()
                    viewModel.loadPembayaranUser(penghuniId)
                } else {
                    Toast.makeText(this@PembayaranUserActivity, "Data penghuni tidak ditemukan. Hubungi admin.", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetch penghuni", e)
                Toast.makeText(this@PembayaranUserActivity, "Gagal memuat data penghuni", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PembayaranUserAdapter(
            onUpload = { pembayaran ->
                val intent = Intent(this, UploadBuktiPembayaranActivity::class.java)
                intent.putExtra("pembayaran_id", pembayaran.id)
                startActivity(intent)
            },
            onDetail = { pembayaran ->
                val intent = Intent(this, PembayaranDetailActivity::class.java)
                intent.putExtra("ID", pembayaran.id)
                startActivity(intent)
            }
        )
        binding.rvPembayaran.layoutManager = LinearLayoutManager(this)
        binding.rvPembayaran.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadPembayaranUser(penghuniId)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeData() {
        viewModel.pembayaranList.observe(this) { list ->
            Log.d(TAG, "Data diterima: ${list?.size ?: 0} item")
            adapter.submitList(list ?: emptyList())
            binding.tvKosong.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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