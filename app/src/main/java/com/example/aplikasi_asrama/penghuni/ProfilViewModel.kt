package com.example.aplikasi_asrama.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class ProfilViewModel : ViewModel() {
    private val apiRepo = ApiRepository()
    private val TAG = "ProfilViewModel"

    private val _penghuni = MutableLiveData<PenghuniData?>()
    val penghuni: LiveData<PenghuniData?> = _penghuni

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadPenghuniByUserId(userId: Int) {
        if (userId <= 0) {
            Log.e(TAG, "loadPenghuniByUserId: userId tidak valid ($userId)")
            _message.value = "User ID tidak valid"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d(TAG, "Memuat data penghuni untuk userId: $userId")
                val data = apiRepo.getPenghuniByUserId(userId)
                _penghuni.value = data
                if (data == null) {
                    Log.w(TAG, "Data penghuni tidak ditemukan untuk userId: $userId")
                    _message.value = "Data penghuni tidak ditemukan"
                } else {
                    Log.d(TAG, "Data penghuni berhasil dimuat: ${data.nama}")
                    _message.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loadPenghuniByUserId: ${e.message}", e)
                _message.value = "Gagal memuat data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateFoto(fotoUri: String?) {
        val current = _penghuni.value
        if (current == null) {
            Log.e(TAG, "updateFoto: Data penghuni tidak tersedia")
            _message.value = "Data penghuni tidak tersedia"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d(TAG, "Mengupdate foto untuk penghuni id=${current.id}, userId=${current.userId}")
                val updated = current.copy(fotoUri = fotoUri)
                val success = apiRepo.editPenghuniJSON(updated)
                if (success) {
                    Log.d(TAG, "Foto berhasil diperbarui")
                    _message.value = "Foto berhasil diperbarui"
                    // Reload data setelah update
                    loadPenghuniByUserId(current.userId)
                } else {
                    Log.e(TAG, "Gagal memperbarui foto")
                    _message.value = "Gagal memperbarui foto"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updateFoto: ${e.message}", e)
                _message.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}