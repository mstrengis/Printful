package com.printful.test.map.view.map

import android.content.Context
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.printful.test.common.dp
import com.printful.test.map.UsersController
import com.printful.test.map.utils.MarkerAnimation
import com.printful.test.map.utils.SimpleRequestListener


class MapContainerView(private val context: Context, private val googleMap: GoogleMap) {

    companion object {

        private val MARKER_SIZE = 30.dp
        private val MARKER_PADDING = 40.dp
    }

    private val adapter = BubbleAdapter(LayoutInflater.from(context))

    init {
        googleMap.setInfoWindowAdapter(adapter)
        googleMap.setOnMapClickListener {
            showAllMarkers()
        }
    }

    private val markerMap = mutableMapOf<Int, Marker>()

    fun drawUsers(list: List<UsersController.State.User>) {
        adapter.users = list
        val latLngBounds = LatLngBounds.Builder()
        var animateToPosition: LatLng? = null
        list.forEach { user ->
            latLngBounds.include(user.position)
            val savedMarker = markerMap[user.id]
            val marker = if (savedMarker == null) {
                val added: Marker? = googleMap.addMarker(
                    MarkerOptions().title(user.id.toString()).position(user.position)
                )

                if (added != null) {
                    markerMap[user.id] = added
                }

                added
            } else {
                savedMarker
            }

            if (marker != null) {
                if (marker.isInfoWindowShown) {
                    marker.showInfoWindow()
                    animateToPosition = user.position
                }

                loadImage(marker, user.image)

                MarkerAnimation.animateMarker(marker, user.position)
            }
        }

        if (animateToPosition != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(animateToPosition))
        } else if (list.isNotEmpty()) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBounds.build(),
                    MARKER_PADDING
                )
            )
        }
    }

    private fun loadImage(marker: Marker, image: String) {
        Glide.with(context)
            .asBitmap()
            .load(image)
            .centerCrop()
            .override(MARKER_SIZE, MARKER_SIZE)
            .apply(RequestOptions().transform(RoundedCorners(MARKER_SIZE / 2)))
            .listener(SimpleRequestListener { bitmap, _ ->
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                marker.setAnchor(0.5f, 0.5f)
            })
            .preload()
    }

    private fun showAllMarkers() {
        if (markerMap.values.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            markerMap.values.forEach {
                boundsBuilder.include(it.position)
            }

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    MARKER_PADDING
                )
            )
        }
    }
}