package com.printful.test.map

import com.google.android.gms.maps.model.LatLng
import org.junit.Test

class MessageParserTest{

    @Test
    fun `parses user list`(){
        val result = MessageParser.parseMessage("USERLIST 1,test name,image3,1.0,1.0;2,test name,image,1.0,1.0;")
        assert(result is MessageParser.Result.UserList)
        assert((result as MessageParser.Result.UserList).users.size == 2)
        val user = result.users[0]
        assert(user.id == 1)
        assert(user.name == "test name")
        assert(user.image == "image3")
        assert(user.position == LatLng(1.0, 1.0))
    }

    @Test
    fun `skips invalid user`(){
        val resultNoName = MessageParser.parseMessage("USERLIST 2,image,1.0,1.0;")
        assert(resultNoName is MessageParser.Result.UserList)
        assert((resultNoName as MessageParser.Result.UserList).users.isEmpty())

        val resultNoLatitude = MessageParser.parseMessage("USERLIST 2,test name,image,x,1.0;")
        assert(resultNoLatitude is MessageParser.Result.UserList)
        assert((resultNoLatitude as MessageParser.Result.UserList).users.isEmpty())

        val resultNoLongitude = MessageParser.parseMessage("USERLIST 2,test name, image,1.0,x;")
        assert(resultNoLongitude is MessageParser.Result.UserList)
        assert((resultNoLongitude as MessageParser.Result.UserList).users.isEmpty())
    }

    @Test
    fun `parses update`(){
        val result = MessageParser.parseMessage("UPDATE 1,2.0,3.0")
        assert(result is MessageParser.Result.Update)
        assert((result as MessageParser.Result.Update).id == 1)
        assert(result.latLng == LatLng(2.0, 3.0))
    }

    @Test
    fun `results in invalid message if update params are absent`(){
        assert(MessageParser.parseMessage("UPDATE 1,3.0") is MessageParser.Result.InvalidMessage)
        assert(MessageParser.parseMessage("UPDATE x,3.0,2.0") is MessageParser.Result.InvalidMessage)
        assert(MessageParser.parseMessage("UPDATE 1,r,2.0") is MessageParser.Result.InvalidMessage)
        assert(MessageParser.parseMessage("UPDATE 1,,2.0") is MessageParser.Result.InvalidMessage)
    }

    @Test
    fun `safely parses invalid message`(){
        val result = MessageParser.parseMessage("UPDATE2 2132,23232")
        assert(result is MessageParser.Result.InvalidMessage)
    }
}