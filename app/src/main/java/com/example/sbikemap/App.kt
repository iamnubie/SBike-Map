package com.example.sbikemap

import android.app.Application
import com.example.sbikemap.data.remote.AppContainer

class App : Application() {

    //  Companion object để truy cập AppContainer từ bất cứ đâu
    companion object {
        lateinit var container: AppContainer
    }

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo AppContainer với Context
        container = AppContainer(applicationContext)
    }
}