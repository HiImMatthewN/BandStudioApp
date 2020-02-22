package com.example.bandstudioapp.Model

class Band(val bandName:String, val location:String,val members:BandMembers
,val genre:Genre, val description:String) {
    constructor():this("","", BandMembers("",""), Genre(""),"")
}
