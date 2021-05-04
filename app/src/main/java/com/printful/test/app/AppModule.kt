package com.printful.test.app

import android.content.Context
import android.location.Geocoder
import com.printful.test.common.di.AppContext
import com.printful.test.common.di.AppScope
import com.printful.test.common.net.RxSocket
import com.printful.test.common.net.TcpRxSocket
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    @AppScope
    @AppContext
    fun context(app: App): Context = app

    @Provides
    @AppScope
    fun geocoder(@AppContext context: Context) = Geocoder(context)

    @AppScope
    @Provides
    fun rxSocket(): RxSocket = TcpRxSocket("ios-test.printful.lv", 6111)
}