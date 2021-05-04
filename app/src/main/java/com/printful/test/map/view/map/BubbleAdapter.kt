package com.printful.test.map.view.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.printful.test.R
import com.printful.test.common.dp
import com.printful.test.map.UsersController
import com.printful.test.map.utils.SimpleRequestListener

class BubbleAdapter(private val inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {

    companion object {

        private val IMAGE_SIZE = 50.dp
    }

    var users: List<UsersController.State.User> = emptyList()

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View {
        val view = inflater.inflate(R.layout.info_window_content, null, false)
        val holder = view.tag as? Holder ?: Holder(view)
        view.tag = holder

        val user = users.firstOrNull { it.id == marker.title?.toIntOrNull() }
        if (user != null) {
            holder.bind(marker, user)
        }
        return view
    }

    private class Holder(view: View) {

        private val avatar = view.findViewById<ImageView>(R.id.avatar)
        private val name = view.findViewById<TextView>(R.id.name)
        private val address = view.findViewById<TextView>(R.id.address)

        fun bind(marker: Marker, user: UsersController.State.User) {
            Glide.with(avatar.context)
                .asBitmap()
                .load(user.image)
                .override(IMAGE_SIZE, IMAGE_SIZE)
                .listener(SimpleRequestListener { _, dataSource ->
                    if (dataSource != DataSource.MEMORY_CACHE) {
                        marker.showInfoWindow()
                    }
                })
                .into(avatar)
            name.text = user.name
            address.text = user.address
        }
    }
}