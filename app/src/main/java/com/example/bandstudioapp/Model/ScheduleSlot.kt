package com.example.bandstudioapp.Model

class ScheduleSlot(val band:ClientTransaction,val startTime:String,val endTime:String) {
    constructor():this(ClientTransaction("","",
        "","",false),"","")

}