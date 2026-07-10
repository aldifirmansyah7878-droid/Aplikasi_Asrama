package com.example.aplikasi_asrama.izin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.databinding.ActivityTambahIzinBinding
import com.example.aplikasi_asrama.api.model.IzinKeluarRequest
import com.example.aplikasi_asrama.repository.ApiRepository
import com.example.aplikasi_asrama.service.TrackingService
import com.example.aplikasi_asrama.utils.LocationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TambahIzinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahIzinBinding
    private lateinit var locationHelper: LocationHelper
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val penghuniId by lazy { prefs.getInt(LoginActivity.KEY_PENGHUNI_ID, 0) }

    private var selectedPenghuni: PenghuniData? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var address: String = ""

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val TAG = "TambahIzin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahIzinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Buat Izin Keluar"

        locationHelper = LocationHelper(this)
        setupSpinner()
        setupDatePickers()
        setupLocation()

        binding.btnSubmit.setOnClickListener {
            submitIzin()
        }
    }

    private fun setupSpinner() {
        if (penghuniId > 0) {
            lifecycleScope.launch {
                try {
                    val penghuni = repository.getPenghuniById(penghuniId)
                    if (penghuni != null) {
                        selectedPenghuni = penghuni
                        val namaList = listOf(penghuni.nama)
                        val adapter = ArrayAdapter(
                            this@TambahIzinActivity,
                            android.R.layout.simple_spinner_item,
                            namaList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.etNamaPenghuni.adapter = adapter
                        binding.etNomorKamar.setText(penghuni.nomorKamar ?: "-")
                    } else {
                        Toast.makeText(this@TambahIzinActivity, "Data penghuni tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error load penghuni", e)
                    Toast.makeText(this@TambahIzinActivity, "Gagal memuat data penghuni", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Penghuni ID tidak ditemukan, hubungi admin", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDatePickers() {
        binding.etTanggalKeluar.setOnClickListener {
            showDatePicker { date ->
                binding.etTanggalKeluar.setText(date)
            }
        }
        binding.etPerkiraanKembali.setOnClickListener {
            showDatePicker { date ->
                binding.etPerkiraanKembali.setText(date)
            }
        }
        binding.etJamKeluar.setOnClickListener {
            showTimePicker { time ->
                binding.etJamKeluar.setText(time)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = Calendar.getInstance()
            date.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = dateFormat.format(date.time)
            onDateSelected(formattedDate)
        }, year, month, day).show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = Calendar.getInstance()
            time.set(Calendar.HOUR_OF_DAY, selectedHour)
            time.set(Calendar.MINUTE, selectedMinute)
            val formattedTime = timeFormat.format(time.time)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }

    private fun setupLocation() {
        locationHelper.getCurrentLocation { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                locationHelper.getAddressFromLocation(latitude, longitude) { addr ->
                    address = addr ?: "Alamat tidak ditemukan"
                    binding.tvAlamat.text = "Lokasi: $address"
                }
            } else {
                binding.tvAlamat.text = "Lokasi: Gagal mendapatkan lokasi"
            }
        }
    }

    private fun submitIzin() {
        val nama = (binding.etNamaPenghuni.selectedItem as? String) ?: ""
        val nomorKamar = binding.etNomorKamar.text.toString().trim()
        val keperluan = binding.etKeperluan.text.toString().trim()
        val tanggalKeluar = binding.etTanggalKeluar.text.toString().trim()
        val jamKeluar = binding.etJamKeluar.text.toString().trim()
        val perkiraanKembali = binding.etPerkiraanKembali.text.toString().trim()
        val letakKunci = binding.etLetakKunci.text.toString().trim()
        val catatanKunci = binding.etCatatanKunci.text.toString().trim()

        if (nama.isEmpty() || nomorKamar.isEmpty() || keperluan.isEmpty() ||
            tanggalKeluar.isEmpty() || jamKeluar.isEmpty() || perkiraanKembali.isEmpty() ||
            letakKunci.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == 0) {
            Toast.makeText(this, "User tidak terdeteksi, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Lokasi belum didapatkan, tunggu sebentar", Toast.LENGTH_SHORT).show()
            return
        }

        val request = IzinKeluarRequest(
            userId = userId,
            namaPenghuni = nama,
            nomorKamar = nomorKamar,
            keperluan = keperluan,
            tanggalKeluar = tanggalKeluar,
            jamKeluar = jamKeluar,
            perkiraanKembali = perkiraanKembali,
            letakKunci = letakKunci,
            catatanKunci = catatanKunci.ifEmpty { null },
            latitude = latitude,
            longitude = longitude,
            alamatLokasi = address
        )

        Log.d(TAG, "Request: userId=$userId, nama=$nama, nomorKamar=$nomorKamar, keperluan=$keperluan, " +
                "tanggalKeluar=$tanggalKeluar, jamKeluar=$jamKeluar, perkiraanKembali=$perkiraanKembali, " +
                "letakKunci=$letakKunci, catatanKunci=$catatanKunci, latitude=$latitude, longitude=$longitude, alamat=$address")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Memanggil repository.createIzinKeluar")
                // ========== PERUBAHAN: menggunakan fungsi yang mengembalikan IzinData? ==========
                val result = repository.createIzinKeluarWithData(request)
                Log.d(TAG, "Hasil: result=$result")
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                if (result != null) {
                    // ========== MULAI TRACKING ==========
                    val newIzinId = result.id
                    Log.d(TAG, "Izin berhasil dibuat dengan ID: $newIzinId")
                    Toast.makeText(this@TambahIzinActivity, "Izin berhasil diajukan", Toast.LENGTH_SHORT).show()

                    // Mulai Tracking Service
                    TrackingService.start(this@TambahIzinActivity, newIzinId, userId)
                    finish()
                } else {
                    Toast.makeText(this@TambahIzinActivity, "Gagal mengajukan izin. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submit izin", e)
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                Toast.makeText(this@TambahIzinActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}