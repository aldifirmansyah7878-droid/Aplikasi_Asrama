package com.example.aplikasi_asrama.ui.izin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasi_asrama.izin.IzinViewModel
import com.example.aplikasi_asrama.repository.ApiRepository

class IzinViewModelFactory(private val repository: ApiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IzinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IzinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}