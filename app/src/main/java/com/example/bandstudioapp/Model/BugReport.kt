package com.example.bandstudioapp.Model

class BugReport(val subject:String,val androidOS:String, val phoneModel:String, val description:String) {
    constructor():this("","","","")
}