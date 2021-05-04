package com.printful.test.map.di

import android.location.Geocoder
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.printful.test.common.di.ActivityScope
import com.printful.test.common.net.RxSocket
import com.printful.test.map.MapViewModel
import com.printful.test.map.UsersController
import com.printful.test.map.geocoder.AddressLookup
import com.printful.test.map.geocoder.GeocoderAddressLookup
import dagger.Module
import dagger.Provides

@Module
class MapModule {

    @Provides
    @ActivityScope
    fun vm(owner: FragmentActivity, factory: MapViewModel.Factory) =
        ViewModelProvider(owner, factory).get(MapViewModel::class.java)

    @Provides
    @ActivityScope
    fun vmFactory(usersController: UsersController) =
        MapViewModel.Factory(usersController)

    @Provides
    fun usersController(rxSocket: RxSocket, addressLookup: AddressLookup) = UsersController(rxSocket, addressLookup)

    @Provides
    fun addressLookup(geocoder: Geocoder): AddressLookup = GeocoderAddressLookup(geocoder)
}