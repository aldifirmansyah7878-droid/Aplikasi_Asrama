package com.example.aplikasi_asrama.izin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasi_asrama.MyApp
import com.example.aplikasi_asrama.databinding.ActivityIzinUserBinding
import com.example.aplikasi_asrama.utils.LocationHelper
import com.example.aplikasi_asrama.api.model.IzinKeluarRequest
import com.example.aplikasi_asrama.ui.izin.IzinViewModelFactory

class IzinUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIzinUserBinding
    private lateinit var viewModel: IzinViewModel
    private lateinit var locationHelper: LocationHelper
    private var currentLocation: Location? = null
    private var address: String = ""

    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIzinUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupLocation()
        setupListeners()
    }

    private fun setupViewModel() {
        val repository = (application as MyApp).apiRepository
        val factory = IzinViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(IzinViewModel::class.java)
        observeOperationResult()
    }

    private fun setupLocation() {
        locationHelper = LocationHelper(this)
        if (checkLocationPermission()) {
            getLocationAndAddress()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndAddress()
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocationAndAddress() {
        locationHelper.getCurrentLocation { location ->
            currentLocation = location
            if (location != null) {
                locationHelper.getAddressFromLocation(location.latitude, location.longitude) { addr ->
                    address = addr ?: "Alamat tidak ditemukan"
                    binding.tvAlamat.text = "Lokasi: $address"
                }
            } else {
                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSubmit.setOnClickListener {
            submitIzin()
        }
    }

    private fun submitIzin() {
        val nama = binding.etNamaPenghuni.text.toString().trim()
        val kamar = binding.etNomorKamar.text.toString().trim()
        val keperluan = binding.etKeperluan.text.toString().trim()
        val tanggalKeluar = binding.etTanggalKeluar.text.toString().trim()
        val jamKeluar = binding.etJamKeluar.text.toString().trim()
        val perkiraanKembali = binding.etPerkiraanKembali.text.toString().trim()
        val letakKunci = binding.etLetakKunci.text.toString().trim()
        val catatanKunci = binding.etCatatanKunci.text.toString().trim()

        if (nama.isEmpty() || kamar.isEmpty() || keperluan.isEmpty() ||
            tanggalKeluar.isEmpty() || jamKeluar.isEmpty() || perkiraanKembali.isEmpty() ||
            letakKunci.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val userid = getSharedPreferences("user_pref", MODE_PRIVATE).getInt("user_id", 0)
        if (userid == 0) {
            Toast.makeText(this, "User tidak terdeteksi", Toast.LENGTH_SHORT).show()
            return
        }

        val location = currentLocation
        if (location == null) {
            Toast.makeText(this, "Lokasi belum didapatkan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = IzinKeluarRequest(
            userId = userid,
            namaPenghuni = nama,
            nomorKamar = kamar,
            keperluan = keperluan,
            tanggalKeluar = tanggalKeluar,
            jamKeluar = jamKeluar,
            perkiraanKembali = perkiraanKembali,
            letakKunci = letakKunci,
            catatanKunci = catatanKunci.ifEmpty { null },
            latitude = location.latitude,
            longitude = location.longitude,
            alamatLokasi = address
        )

        viewModel.createIzinKeluar(request)
    }

    private fun observeOperationResult() {
        viewModel.operationResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Izin berhasil dibuat", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                null -> {}
            }
        }
    }
}