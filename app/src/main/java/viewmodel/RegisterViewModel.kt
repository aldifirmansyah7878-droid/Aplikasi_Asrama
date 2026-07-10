package com.example.aplikasi_asrama.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

sealed class RegisterResult {
    object Loading : RegisterResult()
    data class Success(val message: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val apiRepo = ApiRepository()
    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(username: String, password: String, namaLengkap: String) {
        viewModelScope.launch {
            _registerResult.value = RegisterResult.Loading
            val success = apiRepo.register(username, password, namaLengkap)
            if (success) {
                _registerResult.value = RegisterResult.Success("Pendaftaran berhasil, silakan login")
            } else {
                _registerResult.value = RegisterResult.Error("Pendaftaran gagal, coba lagi")
            }
        }
    }
}