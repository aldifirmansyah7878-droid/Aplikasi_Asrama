package com.example.aplikasi_asrama.keluhan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.KeluhanData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.io.File

sealed class KeluhanUiState {
    object Loading : KeluhanUiState()
    data class Success(val data: List<KeluhanData>) : KeluhanUiState()
    data class Error(val message: String) : KeluhanUiState()
    object Empty : KeluhanUiState()
}

class KeluhanViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _keluhanState = MutableLiveData<KeluhanUiState>()
    val keluhanState: LiveData<KeluhanUiState> = _keluhanState

    private val _detailState = MutableLiveData<KeluhanData?>()
    val detailState: LiveData<KeluhanData?> = _detailState

    // PERBAIKAN: buat nullable agar bisa di-set null
    private val _operationResult = MutableLiveData<Result<Unit>?>()
    val operationResult: LiveData<Result<Unit>?> = _operationResult

    // Ambil semua keluhan (admin)
    fun getAllKeluhan(status: String? = null) {
        viewModelScope.launch {
            _keluhanState.value = KeluhanUiState.Loading
            try {
                val data = repository.getAllKeluhan(status)
                if (data.isEmpty()) {
                    _keluhanState.value = KeluhanUiState.Empty
                } else {
                    _keluhanState.value = KeluhanUiState.Success(data)
                }
            } catch (e: Exception) {
                _keluhanState.value = KeluhanUiState.Error(e.message ?: "Error")
            }
        }
    }

    // Ambil keluhan berdasarkan user
    fun getKeluhanByUser(userId: Int) {
        viewModelScope.launch {
            _keluhanState.value = KeluhanUiState.Loading
            try {
                val data = repository.getKeluhanByUser(userId)
                if (data.isEmpty()) {
                    _keluhanState.value = KeluhanUiState.Empty
                } else {
                    _keluhanState.value = KeluhanUiState.Success(data)
                }
            } catch (e: Exception) {
                _keluhanState.value = KeluhanUiState.Error(e.message ?: "Error")
            }
        }
    }

    // Ambil detail keluhan
    fun getKeluhanById(id: Int) {
        viewModelScope.launch {
            try {
                val data = repository.getKeluhanById(id)
                _detailState.value = data
            } catch (e: Exception) {
                Log.e("KeluhanVM", "getKeluhanById error", e)
            }
        }
    }

    // Kirim keluhan (user)
    fun tambahKeluhan(
        userId: Int,
        namaPenghuni: String,
        nomorKamar: String,
        judulKeluhan: String,
        deskripsiKeluhan: String,
        tanggalKeluhan: String,
        fotoFile: File?
    ) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val success = repository.tambahKeluhan(
                    userId = userId,
                    namaPenghuni = namaPenghuni,  // <-- perbaiki nama parameter
                    nomorKamar = nomorKamar,
                    judulKeluhan = judulKeluhan,
                    deskripsiKeluhan = deskripsiKeluhan,
                    tanggalKeluhan = tanggalKeluhan,
                    fotoFile = fotoFile
                )
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                } else {
                    _operationResult.value = Result.Error("Gagal mengirim keluhan")
                }
            } catch (e: Exception) {
                _operationResult.value = Result.Error(e.message ?: "Error")
            }
        }
    }

    // Tanggapi keluhan (admin)
    fun tanggapiKeluhan(id: Int, tanggapan: String, status: String, fotoPerbaikanFile: File? = null) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val success = repository.tanggapiKeluhan(
                    id = id,
                    tanggapan = tanggapan,
                    status = status,
                    fotoPerbaikanFile = fotoPerbaikanFile
                )
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                } else {
                    _operationResult.value = Result.Error("Gagal mengirim tanggapan")
                }
            } catch (e: Exception) {
                _operationResult.value = Result.Error(e.message ?: "Error")
            }
        }
    }

    // Update status (admin)
    fun updateStatusKeluhan(id: Int, status: String) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val success = repository.updateStatusKeluhan(id, status)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                } else {
                    _operationResult.value = Result.Error("Gagal update status")
                }
            } catch (e: Exception) {
                _operationResult.value = Result.Error(e.message ?: "Error")
            }
        }
    }

    // Hapus keluhan
    fun hapusKeluhan(id: Int) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val success = repository.hapusKeluhan(id)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                } else {
                    _operationResult.value = Result.Error("Gagal hapus keluhan")
                }
            } catch (e: Exception) {
                _operationResult.value = Result.Error(e.message ?: "Error")
            }
        }
    }

    fun resetOperationResult() {
        _operationResult.value = null  // sekarang legal karena nullable
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}