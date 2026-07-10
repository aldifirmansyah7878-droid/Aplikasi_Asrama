package com.example.aplikasi_asrama

import android.app.Application
import com.example.aplikasi_asrama.repository.ApiRepository

class MyApp : Application() {
    lateinit var apiRepository: ApiRepository
        private set

    override fun onCreate() {
        super.onCreate()
        apiRepository = ApiRepository()
    }
}