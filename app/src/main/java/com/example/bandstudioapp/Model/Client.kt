package com.example.bandstudioapp.Model

import com.google.firebase.database.PropertyName

class Client(val uid: String, val bandName: String, val profileImageUrl:String,val token:String,@PropertyName("firstTime")var firstTime:Boolean) {
    constructor():this("","","","", true)

}