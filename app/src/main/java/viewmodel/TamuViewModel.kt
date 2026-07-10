package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.TamuData
import com.example.aplikasi_asrama.api.model.TamuRequest
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class TamuViewModel : ViewModel() {
    private val apiRepo = ApiRepository()

    private val _tamuList = MutableLiveData<List<TamuData>>()
    val tamuList: LiveData<List<TamuData>> = _tamuList

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadAllTamu() {
        viewModelScope.launch {
            _loading.value = true
            val list = apiRepo.getAllTamu()
            _tamuList.value = list
            _loading.value = false
        }
    }

    fun loadTamuByUser(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            val list = apiRepo.getTamuByUser(userId)
            _tamuList.value = list
            _loading.value = false
        }
    }

    fun searchTamu(query: String) {
        viewModelScope.launch {
            _loading.value = true
            val all = apiRepo.getAllTamu()
            val filtered = all.filter {
                it.namaTamu.contains(query, ignoreCase = true) ||
                        it.tujuanPenghuni.contains(query, ignoreCase = true) ||
                        it.nomorKamar.contains(query, ignoreCase = true)
            }
            _tamuList.value = filtered
            _loading.value = false
        }
    }

    fun updateStatusTamu(id: Int, status: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            val success = apiRepo.updateStatusTamu(id, status)
            if (success) {
                _message.value = "Status berhasil diubah"
                loadAllTamu()
            } else {
                _message.value = "Gagal mengubah status"
            }
            _loading.value = false
            onResult(success)
        }
    }

    fun tambahTamu(
        namaTamu: String,
        hubungan: String,
        userid: Int,
        namaPenghuni: String,
        nomorKamar: String,
        waktuKunjungan: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _loading.value = true
            val request = TamuRequest(
                namaTamu = namaTamu,
                hubungan = hubungan,
                userid = userid,
                tujuanPenghuni = namaPenghuni,
                nomorKamar = nomorKamar,
                waktuKunjungan = waktuKunjungan
            )
            val success = apiRepo.tambahTamu(request)
            if (success) {
                _message.value = "Tamu berhasil ditambahkan"
                loadAllTamu()
            } else {
                _message.value = "Gagal menambahkan tamu"
            }
            _loading.value = false
            onResult(success)
        }
    }

    suspend fun getTamuById(id: Int): TamuData? {
        return try {
            _loading.value = true
            apiRepo.getTamuById(id)
        } catch (e: Exception) {
            null
        } finally {
            _loading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}