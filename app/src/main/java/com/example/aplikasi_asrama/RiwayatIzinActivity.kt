package com.example.aplikasi_asrama.izin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.MyApp
import com.example.aplikasi_asrama.databinding.ActivityRiwayatIzinBinding
import com.example.aplikasi_asrama.ui.izin.IzinAdminAdapter
import com.example.aplikasi_asrama.ui.izin.IzinViewModelFactory

class RiwayatIzinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatIzinBinding
    private lateinit var viewModel: IzinViewModel
    private lateinit var adapter: IzinAdminAdapter
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatIzinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Izin"

        // Setup ViewModel
        val repository = (application as MyApp).apiRepository
        val factory = IzinViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(IzinViewModel::class.java)

        // Setup adapter
        adapter = IzinAdminAdapter { izin ->
            DetailIzinActivity.start(this, izin.id)
        }
        binding.rvRiwayatIzin.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatIzin.adapter = adapter

        // Observe data
        viewModel.izinList.observe(this) { list ->
            adapter.submitList(list)
            binding.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.izinState.observe(this) { state ->
            when (state) {
                is IzinUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is IzinUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                }
                is IzinUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvKosong.visibility = View.VISIBLE
                }
                is IzinUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Load data jika userId valid
        if (userId > 0) {
            viewModel.loadIzinByUser(userId)
        } else {
            binding.tvKosong.visibility = View.VISIBLE
            binding.tvKosong.text = "User tidak terdeteksi, silakan login ulang"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}