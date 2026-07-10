package com.example.aplikasi_asrama.tamu

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.LoginActivity
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.api.model.TamuRequest
import com.example.aplikasi_asrama.databinding.ActivityTambahTamuBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TambahTamuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahTamuBinding
    private val repository = ApiRepository()
    private val prefs by lazy { getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE) }
    private val userId by lazy { prefs.getInt(LoginActivity.KEY_USER_ID, 0) }
    private val TAG = "TambahTamu"

    private var penghuniList: List<PenghuniData> = emptyList()
    private var selectedPenghuni: PenghuniData? = null

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahTamuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLihatRiwayat.setOnClickListener {
            startActivity(Intent(this, TamuUserActivity::class.java))
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tambah Tamu"

        loadPenghuni()
        setupDatePicker()
        setupListeners()
    }

    private fun loadPenghuni() {
        lifecycleScope.launch {
            try {
                val penghuni = repository.getPenghuniByUserId(userId)
                if (penghuni != null) {
                    penghuniList = listOf(penghuni)
                    val namaList = penghuniList.map { "${it.nama} - Kamar ${it.nomorKamar}" }
                    val adapter = ArrayAdapter(
                        this@TambahTamuActivity,
                        android.R.layout.simple_spinner_item,
                        namaList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerPenghuni.adapter = adapter
                    selectedPenghuni = penghuni
                } else {
                    Toast.makeText(this@TambahTamuActivity, "Data penghuni tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPenghuni error", e)
                Toast.makeText(this@TambahTamuActivity, "Gagal memuat data penghuni", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatePicker() {
        binding.etWaktuKunjungan.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = Calendar.getInstance()
            date.set(selectedYear, selectedMonth, selectedDay)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val time = Calendar.getInstance()
                time.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                val formattedDateTime = dateTimeFormat.format(time.time)
                binding.etWaktuKunjungan.setText(formattedDateTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, year, month, day).show()
    }

    private fun setupListeners() {
        binding.btnSimpan.setOnClickListener {
            simpanTamu()
        }
    }

    private fun simpanTamu() {
        val namaTamu = binding.etNamaTamu.text.toString().trim()
        val hubungan = binding.etHubungan.text.toString().trim()
        val tujuan = binding.etTujuan.text.toString().trim()
        val waktuKunjungan = binding.etWaktuKunjungan.text.toString().trim()

        Log.d(TAG, "namaTamu: '$namaTamu'")
        Log.d(TAG, "hubungan: '$hubungan'")
        Log.d(TAG, "tujuan: '$tujuan'")
        Log.d(TAG, "waktuKunjungan: '$waktuKunjungan'")
        Log.d(TAG, "userId: $userId")

        if (namaTamu.isEmpty() || hubungan.isEmpty() || tujuan.isEmpty() || waktuKunjungan.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPenghuni == null) {
            Toast.makeText(this, "Pilih penghuni terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TamuRequest(
            namaTamu = namaTamu,
            tujuanPenghuni = tujuan,
            nomorKamar = selectedPenghuni!!.nomorKamar ?: "",
            waktuKunjungan = waktuKunjungan,
            hubungan = hubungan,
            userid = userId
        )

        Log.d(TAG, "Mengirim request: $request")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val success = repository.tambahTamu(request)
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                if (success) {
                    Toast.makeText(this@TambahTamuActivity, "Tamu berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahTamuActivity, "Gagal menambahkan tamu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Log.e(TAG, "simpanTamu error", e)
                Toast.makeText(this@TambahTamuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}