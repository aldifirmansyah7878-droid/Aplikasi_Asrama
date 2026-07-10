package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.KamarData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class KamarViewModel : ViewModel() {
    private val apiRepo = ApiRepository()
    private val _kamarList = MutableLiveData<List<KamarData>>()
    val kamarList: LiveData<List<KamarData>> = _kamarList
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadAllKamar() {
        viewModelScope.launch {
            _loading.value = true
            val list = apiRepo.getAllKamar()
            _kamarList.value = list
            _loading.value = false
        }
    }

    suspend fun getKamarById(id: Int): KamarData? {
        return try {
            _loading.value = true
            apiRepo.getKamarById(id)
        } catch (e: Exception) {
            null
        } finally {
            _loading.value = false
        }
    }

    suspend fun tambahKamar(kamar: KamarData): Boolean {
        return try {
            _loading.value = true
            val success = apiRepo.tambahKamar(kamar)
            if (success) loadAllKamar()
            success
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    suspend fun editKamar(kamar: KamarData): Boolean {
        return try {
            _loading.value = true
            val success = apiRepo.editKamar(kamar)
            if (success) loadAllKamar()
            success
        } catch (e: Exception) {
            _message.value = "Error: ${e.message}"
            false
        } finally {
            _loading.value = false
        }
    }

    fun hapusKamar(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            val success = apiRepo.hapusKamar(id)
            if (success) {
                _message.value = "Kamar berhasil dihapus"
                loadAllKamar()
            } else {
                _message.value = "Gagal menghapus kamar"
            }
            _loading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    
}