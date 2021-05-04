package com.printful.test.app

import android.app.Application
import com.printful.test.BuildConfig
import timber.log.Timber

class App : Application() {

    companion object {

        private lateinit var INSTANCE: App

        fun getComponent(): AppComponent = INSTANCE.component
    }

    private lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
        component = DaggerAppComponent.builder()
            .application(this)
            .build()

        component.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}