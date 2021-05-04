package com.printful.test.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModel(val usersController: UsersController) : ViewModel() {

    override fun onCleared() {
        usersController.clear()
        super.onCleared()
    }

    class Factory(private val usersController: UsersController) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapViewModel(usersController) as T
    }
}