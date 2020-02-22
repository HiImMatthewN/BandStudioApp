package com.example.bandstudioapp.Model

class Schedule(val epouchValue: Long, val slot:MutableList<ScheduleSlot>) {
    constructor():this(0, mutableListOf())
}