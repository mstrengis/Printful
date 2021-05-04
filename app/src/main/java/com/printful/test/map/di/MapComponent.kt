package com.printful.test.map.di

import com.printful.test.app.AppComponent
import com.printful.test.common.di.ActivityModule
import com.printful.test.common.di.ActivityScope
import com.printful.test.map.MapActivity
import dagger.Component

@ActivityScope
@Component(
        dependencies = [AppComponent::class],
        modules = [
            ActivityModule::class,
            MapModule::class,
        ]
)
interface MapComponent {
    fun inject(activity: MapActivity)
}