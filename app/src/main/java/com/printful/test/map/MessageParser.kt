package com.printful.test.map

import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

object MessageParser {

    sealed class Result {
        object InvalidMessage : Result()
        data class UserList(val users: List<UsersController.State.User>) : Result()
        data class Update(val id: Int, val latLng: LatLng) : Result()
    }

    fun parseMessage(update: String): Result {
        return when {
            update.startsWith("USERLIST ") -> parseUserList(update.substring(9))
            update.startsWith("UPDATE ") -> parseUpdate(update.substring(7))
            else -> {
                Timber.w("Error parsing message $update")
                Result.InvalidMessage
            }
        }
    }

    private fun parseUpdate(message: String): Result {
        val updateParts = message.split(",")
        return if (updateParts.size == 3) {
            val id = updateParts[0].toIntOrNull() ?: return Result.InvalidMessage
            val lat = updateParts[1].toDoubleOrNull() ?: return Result.InvalidMessage
            val long = updateParts[2].toDoubleOrNull() ?: return Result.InvalidMessage
            return Result.Update(id, LatLng(lat, long))
        } else {
            Result.InvalidMessage
        }
    }

    private fun parseUserList(message: String): Result {
        val parsedUsers = message.split(";").mapNotNull { userString ->
            val userParts = userString.split(",")
            if (userParts.size == 5) {
                val id = userParts[0].toIntOrNull() ?: return@mapNotNull null
                val name = userParts[1]
                val image = userParts[2]
                val lat = userParts[3].toDoubleOrNull() ?: return@mapNotNull null
                val long = userParts[4].toDoubleOrNull() ?: return@mapNotNull null

                return@mapNotNull UsersController.State.User(id, name, image, LatLng(lat, long))
            }

            return@mapNotNull null
        }

        return Result.UserList(parsedUsers)
    }
}