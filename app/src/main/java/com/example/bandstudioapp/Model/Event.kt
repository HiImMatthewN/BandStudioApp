package com.example.bandstudioapp.Model

class Event(val eventImageUrl:String, val eventName:String , val eventVenue:String, val eventDate:String) {
    constructor():this("","","","")
}