package com.example.bandstudioapp.Model

import com.google.firebase.database.PropertyName

class ClientTransaction(val uid:String, val bandName:String,val profileImage:String,val state:String,@PropertyName("anonymous")val isAnonymous:Boolean) {
    constructor():this("","","","",false)



}