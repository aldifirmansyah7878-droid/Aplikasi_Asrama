package com.example.aplikasi_asrama.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.UserData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

sealed class LoginResult {
    object Loading : LoginResult()
    data class Success(val user: UserData) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel : ViewModel() {
    private val apiRepo = ApiRepository()
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = LoginResult.Loading
            try {
                val response = apiRepo.login(username, password)
                if (response != null) {
                    if (response.status == "success") {
                        if (response.data != null) {
                            _loginResult.value = LoginResult.Success(response.data)
                        } else {
                            _loginResult.value = LoginResult.Error("Data user tidak ditemukan dalam response")
                        }
                    } else {
                        val errorMsg = response.message ?: "Login gagal: ${response.status}"
                        _loginResult.value = LoginResult.Error(errorMsg)
                    }
                } else {
                    _loginResult.value = LoginResult.Error("Gagal terhubung ke server. Periksa koneksi internet Anda.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login error: ${e.message}", e)
                _loginResult.value = LoginResult.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}