package com.example.aplikasi_asrama.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val apiRepo = ApiRepository()

    private val _penghuni = MutableLiveData<PenghuniData?>()
    val penghuni: LiveData<PenghuniData?> = _penghuni

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun loadPenghuniByUserId(userId: Int) {
        if (userId <= 3) {
            _message.value = "User ID tidak valid"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            val data = apiRepo.getPenghuniByUserId(userId)
            _penghuni.value = data
            _loading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}