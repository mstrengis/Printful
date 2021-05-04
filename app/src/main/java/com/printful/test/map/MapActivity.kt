package com.printful.test.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.MapView
import com.printful.test.R
import com.printful.test.app.App
import com.printful.test.common.di.ActivityModule
import com.printful.test.map.di.DaggerMapComponent
import com.printful.test.map.view.error.ErrorEventsToUsersControllerInput
import com.printful.test.map.view.error.ErrorView
import com.printful.test.map.view.error.UsersControllerToErrorViewState
import com.printful.test.map.view.map.getMapContainer
import com.printful.test.map.view.loader.LoaderView
import com.printful.test.map.view.loader.UsersControllerStateToViewState
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class MapActivity : AppCompatActivity() {

    @Inject
    internal lateinit var vm: MapViewModel

    private val root by lazy { findViewById<ConstraintLayout>(R.id.root) }

    private val mapView by lazy { findViewById<MapView>(R.id.mapView) }
    private val mapContainer by lazy { mapView.getMapContainer().toFlowable().cache() }
    private val loader by lazy { LoaderView(root) }
    private val error by lazy { ErrorView(root) }

    private val startStopSubs = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerMapComponent.builder()
            .appComponent(App.getComponent())
            .activityModule(ActivityModule(this))
            .build()
            .inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Flowable.combineLatest(
            mapContainer,
            vm.usersController.states(),
            { mapContainer, state ->
                Pair(mapContainer, state)
            })
            .subscribe(
                { (mapContainer, state) ->
                    mapContainer.drawUsers(state.users)
                },
                Timber::w
            )
            .also(startStopSubs::add)

        vm.usersController.states()
            .compose(UsersControllerStateToViewState())
            .subscribe(loader::render, Timber::w)
            .also(startStopSubs::add)

        vm.usersController.states()
            .compose(UsersControllerToErrorViewState())
            .subscribe(error::render, Timber::w)
            .also(startStopSubs::add)

        error.events()
            .compose(ErrorEventsToUsersControllerInput())
            .subscribe(vm.usersController::input, Timber::w)
            .also(startStopSubs::add)

        vm.usersController.input(UsersController.Input.StartMonitoring)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        startStopSubs.clear()
        vm.usersController.input(UsersController.Input.StopMonitoring)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}