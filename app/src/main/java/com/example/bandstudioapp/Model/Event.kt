package com.example.bandstudioapp.Model

class Event(val eventPosterUrl:String, val eventName:String
            , val eventVenue:String, val eventDate:String
            , val eventPrice:Int, val productionName:String, val eventTime:String) {
    constructor():this("","",""
        ,"",0,"","")
}