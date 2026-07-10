package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File

class PembayaranViewModel : ViewModel() {
    private val repo = ApiRepository()

    // ============= LiveData =============
    private val _pembayaranList = MutableLiveData<List<PembayaranData>>()
    val pembayaranList: LiveData<List<PembayaranData>> = _pembayaranList

    private val _pembayaranAdmin = MutableLiveData<List<PembayaranData>>()
    val pembayaranAdmin: LiveData<List<PembayaranData>> = _pembayaranAdmin

    private val _pembayaranDetail = MutableLiveData<PembayaranData?>()
    val pembayaranDetail: LiveData<PembayaranData?> = _pembayaranDetail

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ============= Fungsi untuk User =============
    fun loadPembayaranUser(penghuniId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pembayaranList.value = repo.getPembayaranUser(penghuniId)
                _message.value = null
            } catch (e: Exception) {
                _message.value = "Gagal memuat tagihan: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchPembayaran(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pembayaranList.value = repo.searchPembayaran(query)
                _message.value = null
            } catch (e: Exception) {
                _message.value = "Gagal mencari: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= Fungsi untuk Admin =============
    fun loadPembayaranAdmin(status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val list = repo.getPembayaranAdmin(status)
                _pembayaranAdmin.value = list
                _message.value = null
            } catch (e: Exception) {
                _message.value = "Gagal memuat data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= Detail =============
    fun loadPembayaranById(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pembayaranDetail.value = repo.getPembayaranById(id)
                _message.value = null
            } catch (e: Exception) {
                _message.value = "Gagal memuat detail: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= Admin Tambah Pembayaran (tanpa file) =============
    fun tambahPembayaranAdmin(
        penghuni_Id:Int,
        bulanTahun: String,
        jumlah: Int,
        metode: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repo.tambahPembayaranAdmin(penghuni_Id, bulanTahun, jumlah, metode)
                _message.value = if (success) "Pembayaran berhasil ditambahkan" else "Gagal menambahkan pembayaran"
                onResult(success)
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= User Upload Bukti (dengan file) =============
    fun tambahPembayaranUser(
        penghuniId: Int,
        bulanTahun: String,
        jumlah: Int,
        metode: String,
        file: File,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repo.tambahPembayaranUser(penghuniId, bulanTahun, jumlah, metode, file)
                _message.value = if (success) "Pembayaran diajukan" else "Gagal mengirim"
                onResult(success)
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadBuktiPembayaran(
        id: Int,
        file: File,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repo.uploadBuktiPembayaran(id, file)
                _message.value = if (success) "Bukti berhasil diupload" else "Gagal upload bukti"
                onResult(success)
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= Admin Verifikasi =============
    fun verifikasiPembayaran(
        id: Int,
        status: String,
        catatan: String,
        admin: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Perbaiki: panggil verifikasiPembayaranAdmin dari repository
                val success = repo.verifikasiPembayaranAdmin(id, status, catatan, admin)
                _message.value = if (success) "Verifikasi berhasil" else "Verifikasi gagal"
                onResult(success)
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    // ============= Helper =============
    fun clearMessage() {
        _message.value = null
    }
}