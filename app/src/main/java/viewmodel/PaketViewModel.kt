package com.example.aplikasi_asrama.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.PaketData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class PaketViewModel(private val repository: ApiRepository = ApiRepository()) : ViewModel() {

    companion object {
        private const val TAG = "PaketViewModel"
    }

    // ===== INISIALISASI DENGAN EMPTY LIST =====
    private val _paketList = MutableLiveData<List<PaketData>>(emptyList())
    val paketList: LiveData<List<PaketData>> = _paketList

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private var allPaket: List<PaketData> = emptyList()

    fun loadAllPaket() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = repository.getAllPaket()
                allPaket = data
                _paketList.value = data
            } catch (e: Exception) {
                _message.value = "Gagal memuat data: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun searchPaket(query: String) {
        val filtered = allPaket.filter {
            it.namaPenghuni.contains(query, ignoreCase = true) ||
                    it.namaPengirim.contains(query, ignoreCase = true) ||
                    it.jenisPaket.contains(query, ignoreCase = true)
        }
        _paketList.value = filtered
        Log.d(TAG, "searchPaket: ${filtered.size} results for '$query'")
    }

    fun updateStatusPaket(id: Int, status: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.updateStatusPaket(id, status)
                callback(success)
                if (success) {
                    _message.value = "Status berhasil diupdate"
                    loadAllPaket()
                } else {
                    _message.value = "Gagal update status"
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateStatusPaket error", e)
                _message.value = "Error: ${e.message}"
                callback(false)
            }
            _loading.value = false
        }
    }

    fun hapusPaket(id: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.hapusPaket(id)
                callback(success)
                if (success) {
                    _message.value = "Paket berhasil dihapus"
                    loadAllPaket()
                } else {
                    _message.value = "Gagal hapus paket"
                }
            } catch (e: Exception) {
                Log.e(TAG, "hapusPaket error", e)
                _message.value = "Error: ${e.message}"
                callback(false)
            }
            _loading.value = false
        }
    }

    // ===== FUNGSI UNTUK MEMBERSIHKAN PESAN =====
    fun clearMessage() {
        _message.value = null
    }

    // ===== RESET STATE =====
    fun reset() {
        _paketList.value = emptyList()
        allPaket = emptyList()
        _loading.value = false
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ViewModel destroyed")
        reset()
    }
}