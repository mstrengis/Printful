package com.printful.test.map.geocoder

import android.location.Address
import android.location.Geocoder
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GeocoderAddressLookup(private val geocoder: Geocoder) : AddressLookup {

    override fun getAddress(latitude: Double, longitude: Double): Single<AddressLookup.Result> {
        return Single
            .create<List<Address>> { emitter ->
                val result = geocoder.getFromLocation(latitude, longitude, 10)
                emitter.onSuccess(result)
            }
            .map(::parseGeocoderResult)
            .subscribeOn(Schedulers.io())
    }

    private fun parseGeocoderResult(result: List<Address>): AddressLookup.Result {
        val address = result.firstOrNull() ?: return AddressLookup.Result.Error
        val addressLines = StringBuilder("")

        for (i in 0..address.maxAddressLineIndex) {
            addressLines.append(address.getAddressLine(i)).append(" ")
        }

        return AddressLookup.Result.Address(addressLines.toString())
    }
}