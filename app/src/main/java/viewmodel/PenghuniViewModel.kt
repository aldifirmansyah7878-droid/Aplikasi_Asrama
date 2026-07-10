package com.example.aplikasi_asrama.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File

class PenghuniViewModel(application: Application) : AndroidViewModel(application) {
    private val apiRepo = ApiRepository()
    private val _penghuniList = MutableLiveData<List<PenghuniData>>()
    val penghuniList: LiveData<List<PenghuniData>> = _penghuniList
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadAllPenghuni() {
        viewModelScope.launch {
            _loading.value = true
            val list = apiRepo.getAllPenghuni()
            _penghuniList.value = list
            _loading.value = false
        }
    }

    suspend fun tambahPenghuni(penghuni: PenghuniData): Boolean {
        return try {
            _loading.value = true
            val success = apiRepo.tambahPenghuniJSON(penghuni)
            if (success) {
                loadAllPenghuni()
                _message.value = "Penghuni berhasil ditambahkan"
            } else {
                _message.value = "Gagal menambahkan penghuni"
            }
            success
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun editPenghuni(penghuni: PenghuniData): Boolean {
        return try {
            _loading.value = true
            val success = apiRepo.editPenghuniJSON(penghuni)
            if (success) {
                loadAllPenghuni()
                _message.value = "Penghuni berhasil diupdate"
            } else {
                _message.value = "Gagal mengupdate penghuni"
            }
            success
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    fun hapusPenghuni(id: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            val success = apiRepo.hapusPenghuni(id)
            if (success) {
                loadAllPenghuni()
                _message.value = "Penghuni berhasil dihapus"
            } else {
                _message.value = "Gagal menghapus penghuni"
            }
            _loading.value = false
            onResult(success)
        }
    }

    suspend fun getPenghuniByUserId(userId: Int): PenghuniData? {
        return try {
            _loading.value = true
            apiRepo.getPenghuniByUserId(userId)
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            null
        } finally {
            _loading.value = false
        }
    }

    // Perbaikan: konversi String? ke File? lalu panggil overload File
    suspend fun updateFotoPenghuni(penghuniId: Int, fotoUri: String?): Boolean {
        return try {
            _loading.value = true
            val file = fotoUri?.let { File(it) }
            val success = apiRepo.updateFotoPenghuni(penghuniId, file)
            if (success) {
                loadAllPenghuni()
                _message.value = "Foto profil berhasil diperbarui"
            } else {
                _message.value = "Gagal memperbarui foto"
            }
            success
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun getPenghuniById(id: Int): PenghuniData? {
        return try {
            _loading.value = true
            Log.d("PenghuniViewModel", "getPenghuniById id: $id")
            val result = apiRepo.getPenghuniById(id)
            Log.d("PenghuniViewModel", "Result: $result")
            result
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            null
        } finally {
            _loading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}