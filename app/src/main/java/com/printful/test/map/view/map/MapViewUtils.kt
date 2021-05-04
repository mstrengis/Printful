package com.printful.test.map.view.map

import com.google.android.gms.maps.MapView
import io.reactivex.Single

fun MapView.getMapContainer(): Single<MapContainerView> {
    return Single.create { containerEmitter ->
        getMapAsync {
            containerEmitter.onSuccess(MapContainerView(context, it))
        }
    }
}