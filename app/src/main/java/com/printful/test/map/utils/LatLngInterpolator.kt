package com.printful.test.map.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*


interface LatLngInterpolator {
    fun interpolate(fraction: Float, from: LatLng, to: LatLng): LatLng
    class Spherical : LatLngInterpolator {
        override fun interpolate(fraction: Float, from: LatLng, to: LatLng): LatLng {
            // http://en.wikipedia.org/wiki/Slerp
            val fromLat: Double = Math.toRadians(from.latitude)
            val fromLng: Double = Math.toRadians(from.longitude)
            val toLat: Double = Math.toRadians(to.latitude)
            val toLng: Double = Math.toRadians(to.longitude)
            val cosFromLat: Double = cos(fromLat)
            val cosToLat: Double = cos(toLat)

            // Computes Spherical interpolation coefficients.
            val angle = computeAngleBetween(fromLat, fromLng, toLat, toLng)
            val sinAngle: Double = sin(angle)
            if (sinAngle < 1E-6) {
                return from
            }
            val a: Double = sin((1 - fraction) * angle) / sinAngle
            val b: Double = sin(fraction * angle) / sinAngle

            // Converts from polar to vector and interpolate.
            val x: Double = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng)
            val y: Double = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng)
            val z: Double = a * sin(fromLat) + b * sin(toLat)

            // Converts interpolated vector back to polar.
            val lat: Double = atan2(z, sqrt(x * x + y * y))
            val lng: Double = atan2(y, x)
            return LatLng(Math.toDegrees(lat), Math.toDegrees(lng))
        }

        private fun computeAngleBetween(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double
        ): Double {
            // Haversine's formula
            val dLat = fromLat - toLat
            val dLng = fromLng - toLng
            return 2 * asin(
                sqrt(
                    sin(dLat / 2).pow(2.0) +
                            cos(fromLat) * cos(toLat) * sin(dLng / 2).pow(2.0)
                )
            )
        }
    }
}