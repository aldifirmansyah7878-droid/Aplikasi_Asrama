package com.example.aplikasi_asrama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasi_asrama.api.model.PembayaranData
import com.example.aplikasi_asrama.repository.ApiRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class LaporanViewModel : ViewModel() {
    private val apiRepo = ApiRepository()

    private val _laporanList = MutableLiveData<List<PembayaranData>>()
    val laporanList: LiveData<List<PembayaranData>> = _laporanList

    private val _total = MutableLiveData<Long>()
    val total: LiveData<Long> = _total

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadLaporan(tahun: String?, bulan: String?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val semua = apiRepo.getAllPembayaran()
                val filtered = semua.filter { pembayaran ->
                    val bulanTahunStr = pembayaran.bulanTahun ?: ""
                    val tahunPembayaran = bulanTahunStr.substringAfterLast(" ").takeIf { it.isNotEmpty() } ?: ""
                    val bulanPembayaran = bulanTahunStr.substringBeforeLast(" ").takeIf { it.isNotEmpty() } ?: ""
                    val tahunCocok = tahun == null || tahun == "Semua Tahun" || tahunPembayaran == tahun
                    val bulanCocok = bulan == null || bulanPembayaran == bulan
                    tahunCocok && bulanCocok
                }
                _laporanList.value = filtered
                val totalPendapatan = filtered.sumOf { it.jumlah.toLong() }
                _total.value = totalPendapatan
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun formatTotal(): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(_total.value ?: 0)}"
    }

    fun clearError() { _error.value = null }
}