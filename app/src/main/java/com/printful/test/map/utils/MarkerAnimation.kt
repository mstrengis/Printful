package com.printful.test.map.utils

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.util.Property
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.printful.test.map.utils.LatLngInterpolator.Spherical

object MarkerAnimation {

    private val latLngInterpolator: LatLngInterpolator = Spherical()

    fun animateMarker(marker: Marker, to: LatLng) {
        val typeEvaluator =
            TypeEvaluator<LatLng> { fraction, startValue, endValue ->
                latLngInterpolator.interpolate(
                    fraction,
                    startValue,
                    endValue
                )
            }
        val property = Property.of(Marker::class.java, LatLng::class.java, "position")

        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, to)
        animator.duration = 300
        animator.start()
    }
}