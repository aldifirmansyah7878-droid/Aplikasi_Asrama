package com.example.aplikasi_asrama.izin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.IzinData
import com.example.aplikasi_asrama.api.model.IzinKeluarRequest
import com.example.aplikasi_asrama.api.model.UpdateIzinRequest
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch

sealed class IzinUiState {
    object Loading : IzinUiState()
    data class Success(val data: List<IzinData>) : IzinUiState()
    data class DetailSuccess(val data: IzinData) : IzinUiState()
    data class Error(val message: String) : IzinUiState()
    object Empty : IzinUiState()
}

class IzinViewModel(private val repository: ApiRepository) : ViewModel() {

    companion object {
        private const val TAG = "IzinViewModel"
    }

    private val _izinState = MutableLiveData<IzinUiState>()
    val izinState: LiveData<IzinUiState> = _izinState

    private val _detailState = MutableLiveData<IzinUiState>()
    val detailState: LiveData<IzinUiState> = _detailState

    // ===== PERBAIKAN: ubah tipe menjadi nullable agar bisa di-set null =====
    private val _operationResult = MutableLiveData<Result<Unit>?>()
    val operationResult: LiveData<Result<Unit>?> = _operationResult

    // State untuk list izin user (Riwayat)
    private val _izinList = MutableLiveData<List<IzinData>>(emptyList())
    val izinList: LiveData<List<IzinData>> = _izinList

    // ========== FUNGSI ==========

    fun fetchAllIzin(status: String? = null) {
        viewModelScope.launch {
            Log.d(TAG, "fetchAllIzin: status=$status")
            _izinState.value = IzinUiState.Loading
            try {
                val data = repository.getAllIzinRest(status)
                if (data.isEmpty()) {
                    _izinState.value = IzinUiState.Empty
                } else {
                    _izinState.value = IzinUiState.Success(data)
                }
                _izinList.value = data
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllIzin error", e)
                _izinState.value = IzinUiState.Error("Gagal memuat data izin: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun loadIzinByUser(userId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "loadIzinByUser: userId=$userId")
            _izinState.value = IzinUiState.Loading
            try {
                if (userId <= 0) {
                    _izinState.value = IzinUiState.Error("User ID tidak valid")
                    _izinList.value = emptyList()
                    return@launch
                }
                val data = repository.getIzinByUser(userId)
                if (data.isEmpty()) {
                    _izinState.value = IzinUiState.Empty
                } else {
                    _izinState.value = IzinUiState.Success(data)
                }
                _izinList.value = data
            } catch (e: Exception) {
                Log.e(TAG, "loadIzinByUser error", e)
                _izinState.value = IzinUiState.Error("Gagal memuat riwayat izin: ${e.message ?: "Terjadi kesalahan"}")
                _izinList.value = emptyList()
            }
        }
    }

    fun fetchIzinDetail(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "fetchIzinDetail: id=$id")
            _detailState.value = IzinUiState.Loading
            try {
                val data = repository.getIzinByIdRest(id)
                if (data != null) {
                    _detailState.value = IzinUiState.DetailSuccess(data)
                } else {
                    _detailState.value = IzinUiState.Error("Data izin tidak ditemukan")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchIzinDetail error", e)
                _detailState.value = IzinUiState.Error("Gagal memuat detail izin: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun createIzinKeluar(request: IzinKeluarRequest) {
        viewModelScope.launch {
            Log.d(TAG, "createIzinKeluar: userid=${request.userId}")
            _operationResult.value = Result.Loading
            try {
                val success = repository.createIzinKeluar(request)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                    Log.d(TAG, "createIzinKeluar: success")
                } else {
                    _operationResult.value = Result.Error("Gagal membuat izin. Silakan coba lagi.")
                    Log.e(TAG, "createIzinKeluar: failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "createIzinKeluar error", e)
                _operationResult.value = Result.Error("Gagal membuat izin: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun returnIzin(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "returnIzin: id=$id")
            _operationResult.value = Result.Loading
            try {
                val success = repository.returnIzin(id)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                    Log.d(TAG, "returnIzin: success")
                } else {
                    _operationResult.value = Result.Error("Gagal mencatat kembali. Silakan coba lagi.")
                    Log.e(TAG, "returnIzin: failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "returnIzin error", e)
                _operationResult.value = Result.Error("Gagal mencatat kembali: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun updateIzin(id: Int, request: UpdateIzinRequest) {
        viewModelScope.launch {
            Log.d(TAG, "updateIzin: id=$id")
            _operationResult.value = Result.Loading
            try {
                val success = repository.updateIzin(id, request)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                    Log.d(TAG, "updateIzin: success")
                } else {
                    _operationResult.value = Result.Error("Gagal memperbarui izin. Silakan coba lagi.")
                    Log.e(TAG, "updateIzin: failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateIzin error", e)
                _operationResult.value = Result.Error("Gagal memperbarui izin: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun deleteIzin(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "deleteIzin: id=$id")
            _operationResult.value = Result.Loading
            try {
                val success = repository.deleteIzin(id)
                if (success) {
                    _operationResult.value = Result.Success(Unit)
                    Log.d(TAG, "deleteIzin: success")
                } else {
                    _operationResult.value = Result.Error("Gagal menghapus izin. Silakan coba lagi.")
                    Log.e(TAG, "deleteIzin: failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteIzin error", e)
                _operationResult.value = Result.Error("Gagal menghapus izin: ${e.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    fun resetOperationResult() {
        _operationResult.value = null
        Log.d(TAG, "resetOperationResult: cleared")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ViewModel destroyed")
        _operationResult.value = null
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}