package com.example.trdial

import android.app.Application
import com.example.trdial.di.AppContainer

class TrDialApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
