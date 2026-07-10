package com.example.aplikasi_asrama.penghuni

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.api.model.UserData
import com.example.aplikasi_asrama.databinding.ActivityTambahEditPenghuniBinding
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahEditPenghuniActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahEditPenghuniBinding
    private var userList = listOf<UserData>()
    private var editingId: Int? = null
    private val TAG = "TambahEditPenghuni"
    private val calendar = Calendar.getInstance()
    private var isSpinnerReady = false
    private var pendingUserIdForSelection: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahEditPenghuniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("penghuni_id", -1)
        editingId = if (id != -1) id else null

        supportActionBar?.title = if (editingId == null) "Tambah Penghuni" else "Edit Penghuni"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDatePickers()
        loadUserSpinner { success ->
            if (success && editingId != null) {
                loadPenghuniData(editingId!!)
            }
        }

        binding.btnSimpan.setOnClickListener { simpan() }
    }

    private fun setupDatePickers() {
        binding.etTanggalMasuk.setOnClickListener { showDatePicker(binding.etTanggalMasuk) }
        binding.etTanggalKeluar.setOnClickListener { showDatePicker(binding.etTanggalKeluar) }
        binding.etTanggalMasuk.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker(binding.etTanggalMasuk)
        }
        binding.etTanggalKeluar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker(binding.etTanggalKeluar)
        }
    }

    private fun showDatePicker(editText: com.google.android.material.textfield.TextInputEditText) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadUserSpinner(onComplete: (Boolean) -> Unit = {}) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "loadUserSpinner: mulai mengambil user")
                val repository = ApiRepository()
                userList = repository.getAllUsers()
                Log.d(TAG, "loadUserSpinner: jumlah user = ${userList.size}")
                if (userList.isNotEmpty()) {
                    for (user in userList) {
                        Log.d(TAG, "   User: id=${user.id}, username=${user.username}, nama=${user.namaLengkap}")
                    }
                }

                if (userList.isEmpty()) {
                    Toast.makeText(
                        this@TambahEditPenghuniActivity,
                        "Tidak ada user dengan role 'user'. Silakan registrasi user biasa terlebih dahulu.",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete(false)
                    return@launch
                }

                val adapter = ArrayAdapter(
                    this@TambahEditPenghuniActivity,
                    android.R.layout.simple_spinner_item,
                    userList.map { "${it.username} - ${it.namaLengkap}" }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerUser.adapter = adapter
                isSpinnerReady = true

                pendingUserIdForSelection?.let { userId ->
                    setSpinnerSelectionByUserId(userId)
                    pendingUserIdForSelection = null
                }
                onComplete(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error loadUserSpinner: ${e.message}", e)
                Toast.makeText(
                    this@TambahEditPenghuniActivity,
                    "Gagal memuat daftar user: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                onComplete(false)
            }
        }
    }

    private fun loadPenghuniData(penghuniId: Int) {
        lifecycleScope.launch {
            try {
                val repository = ApiRepository()
                val penghuni = repository.getPenghuniById(penghuniId)
                if (penghuni != null) {
                    binding.etNama.setText(penghuni.nama)
                    binding.etNim.setText(penghuni.Nim)
                    binding.etNoTelp.setText(penghuni.noTelp)
                    binding.etAlamat.setText(penghuni.alamatAsal)
                    binding.etNomorKamar.setText(penghuni.nomorKamar)
                    binding.etTanggalMasuk.setText(penghuni.tanggalMasuk)
                    binding.etTanggalKeluar.setText(penghuni.tanggalKeluar ?: "")

                    if (isSpinnerReady) {
                        setSpinnerSelectionByUserId(penghuni.userId)
                    } else {
                        pendingUserIdForSelection = penghuni.userId
                    }
                } else {
                    Toast.makeText(
                        this@TambahEditPenghuniActivity,
                        "Data penghuni tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loadPenghuniData: ${e.message}", e)
                Toast.makeText(
                    this@TambahEditPenghuniActivity,
                    "Gagal memuat data penghuni",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setSpinnerSelectionByUserId(userId: Int) {
        val index = userList.indexOfFirst { it.id == userId }
        if (index >= 0) {
            binding.spinnerUser.setSelection(index)
            Log.d(TAG, "Spinner user dipilih index $index untuk userId $userId")
        } else {
            Log.w(TAG, "Tidak ditemukan user dengan id $userId di userList")
            // Fallback: pilih yang pertama jika tidak ditemukan
            if (userList.isNotEmpty()) {
                binding.spinnerUser.setSelection(0)
                Log.d(TAG, "Fallback: pilih user pertama")
            }
        }
    }

    private fun simpan() {
        if (userList.isEmpty()) {
            Toast.makeText(this, "Daftar user belum dimuat", Toast.LENGTH_SHORT).show()
            return
        }
        val pos = binding.spinnerUser.selectedItemPosition
        if (pos < 0 || pos >= userList.size) {
            Toast.makeText(this, "Pilih akun penghuni", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedUser = userList[pos]
        if (selectedUser.id <= 0) {
            Toast.makeText(this, "User ID tidak valid (${selectedUser.id})", Toast.LENGTH_SHORT).show()
            return
        }
        val nama = binding.etNama.text.toString().trim()
        val nim = binding.etNim.text.toString().trim()
        val noTelp = binding.etNoTelp.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val nomorKamar = binding.etNomorKamar.text.toString().trim()
        val tanggalMasuk = binding.etTanggalMasuk.text.toString().trim()
        val tanggalKeluar = binding.etTanggalKeluar.text.toString().trim().ifEmpty { null }

        if (nama.isEmpty() || nim.isEmpty() || noTelp.isEmpty() || alamat.isEmpty() || nomorKamar.isEmpty() || tanggalMasuk.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val penghuni = PenghuniData(
            id = editingId ?: 0,
            userId = selectedUser.id,
            username = selectedUser.username,
            nama = nama,
            Nim = nim,
            noTelp = noTelp,
            alamatAsal = alamat,
            kamarId = 0,
            nomorKamar = nomorKamar,
            tanggalMasuk = tanggalMasuk,
            tanggalKeluar = tanggalKeluar,
            status = "aktif",
            fotoUri = null
        )

        Log.d(TAG, "Data yang dikirim: userId=${penghuni.userId}, nama=$nama, nim=$nim, noTelp=$noTelp, alamat=$alamat, nomorKamar=$nomorKamar, tanggalMasuk=$tanggalMasuk")

        lifecycleScope.launch {
            try {
                val repository = ApiRepository()
                val success = if (editingId == null) {
                    repository.tambahPenghuniJSON(penghuni)
                } else {
                    repository.editPenghuniJSON(penghuni)
                }
                if (success) {
                    Toast.makeText(this@TambahEditPenghuniActivity, "Data tersimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahEditPenghuniActivity, "Gagal menyimpan. Cek koneksi atau log server.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error simpan: ${e.message}", e)
                Toast.makeText(this@TambahEditPenghuniActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}