package com.example.aplikasi_asrama.paket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.databinding.ActivityAdminPaketBinding
import com.example.aplikasi_asrama.helper.NotifikasiHelper  // <-- TAMBAHKAN
import com.example.aplikasi_asrama.viewmodel.PaketViewModel

class AdminPaketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPaketBinding
    private lateinit var viewModel: PaketViewModel
    private lateinit var adapter: PaketAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPaketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Data Paket"

        viewModel = ViewModelProvider(this)[PaketViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        observeData()

        binding.fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahPaketActivity::class.java))
        }

        viewModel.loadAllPaket()
    }

    private fun setupRecyclerView() {
        adapter = PaketAdminAdapter(
            onDetail = { paket ->
                startActivity(Intent(this, DetailPaketActivity::class.java).apply {
                    putExtra("PAKET_ID", paket.id)
                })
            },
            onStatusUpdate = { paket, status ->
                viewModel.updateStatusPaket(paket.id, status) { success ->
                    if (success) {
                        Toast.makeText(this, "Status diupdate", Toast.LENGTH_SHORT).show()
                        viewModel.loadAllPaket()

                        // ===== KIRIM NOTIFIKASI =====
                        if (status == "diambil" || status == "selesai") {
                            // Ambil userId dari paket (paket.userid)
                            NotifikasiHelper.paketBaru(
                                this@AdminPaketActivity,
                                paket.userid,
                                paket.namaPenghuni,
                                paket.jenisPaket,
                                paket.namaPengirim,
                                paket.id
                            )
                        }
                    } else {
                        Toast.makeText(this, "Gagal update", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvPaket.layoutManager = LinearLayoutManager(this)
        binding.rvPaket.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(query: String?): Boolean {
                if (query.isNullOrBlank()) viewModel.loadAllPaket()
                else viewModel.searchPaket(query)
                return true
            }
        })
    }

    private fun observeData() {
        viewModel.paketList.observe(this) { list ->
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