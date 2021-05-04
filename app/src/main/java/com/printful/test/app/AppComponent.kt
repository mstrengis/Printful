package com.printful.test.app

import android.content.Context
import android.location.Geocoder
import com.printful.test.common.di.AppContext
import com.printful.test.common.di.AppScope
import com.printful.test.common.net.RxSocket
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    modules = [
        AppModule::class,
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: App): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)

    fun geocoder(): Geocoder
    fun rxSocket(): RxSocket
    @AppContext fun context(): Context
}
