package com.printful.test.map.geocoder

import io.reactivex.Single

interface AddressLookup {

    sealed class Result {
        data class Address(val address: String) : Result()
        object Error : Result()
    }

    fun getAddress(latitude: Double, longitude: Double): Single<Result>
}