package com.example.aplikasi_asrama.izin

import android.os.Bundle
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplikasi_asrama.MyApp
import com.example.aplikasi_asrama.databinding.ActivityIzinAdminBinding
import com.example.aplikasi_asrama.ui.izin.IzinAdminAdapter
import com.example.aplikasi_asrama.ui.izin.IzinViewModelFactory

class IzinAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIzinAdminBinding
    private lateinit var viewModel: IzinViewModel
    private lateinit var adapter: IzinAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIzinAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupObserver()
        setupSpinnerFilter()

        viewModel.fetchAllIzin()
    }

    private fun setupViewModel() {
        val repository = (application as MyApp).apiRepository
        val factory = IzinViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(IzinViewModel::class.java)
    }

    private fun setupRecyclerView() {
        adapter = IzinAdminAdapter { izin ->
            DetailIzinActivity.start(this, izin.id)
        }
        binding.rvIzin.apply {
            layoutManager = LinearLayoutManager(this@IzinAdminActivity)
            adapter = this@IzinAdminActivity.adapter
        }
    }

    private fun setupObserver() {
        viewModel.izinState.observe(this) { state ->
            when (state) {
                is IzinUiState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is IzinUiState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    adapter.submitList(state.data)
                }
                is IzinUiState.Empty -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    adapter.submitList(emptyList())
                    Toast.makeText(this, "Tidak ada data", Toast.LENGTH_SHORT).show()
                }
                is IzinUiState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupSpinnerFilter() {
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val status = when (position) {
                    0 -> null
                    1 -> "keluar"
                    2 -> "kembali"
                    else -> null
                }
                viewModel.fetchAllIzin(status)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}