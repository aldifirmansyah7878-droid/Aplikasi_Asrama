package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class UserDashboardViewModel : ViewModel() {
    private val repo = ApiRepository()

    private val _penghuniData = MutableLiveData<PenghuniData?>()
    val penghuniData: LiveData<PenghuniData?> = _penghuniData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPenghuniData(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = repo.getPenghuniByUserId(userId)
                _penghuniData.value = data
                if (data == null) {
                    _error.value = "Data penghuni tidak ditemukan"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}