package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.DashboardOverview
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _dashboardData = MutableLiveData<DashboardOverview?>()
    val dashboardData: LiveData<DashboardOverview?> = _dashboardData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboard() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = repository.getDashboardOverview()
                _dashboardData.value = data
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Gagal memuat dashboard: ${e.message}"
            }
            _loading.value = false
        }
    }
}