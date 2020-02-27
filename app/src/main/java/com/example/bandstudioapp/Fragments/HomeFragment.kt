package com.example.bandstudioapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.GroupieAdapter.ConfirmerScheduleRow
import com.example.bandstudioapp.GroupieAdapter.PendingScheduleRow
import com.example.bandstudioapp.Model.ClientTransaction
import com.example.bandstudioapp.Model.ScheduleSlot
import com.example.bandstudioapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home.*


val pendingRVadapter = GroupAdapter<GroupieViewHolder>()
val confirmedRVAdapter = GroupAdapter<GroupieViewHolder>()
private lateinit var auth:FirebaseAuth
private lateinit var database:FirebaseDatabase
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        pending_books_recyclerView.adapter = pendingRVadapter
        confirmed_books_recyclerView.adapter = confirmedRVAdapter
        showPendingSchedules()








    }


    private fun showPendingSchedules(){

        val uid = auth.currentUser?.uid
        val ref = database.getReference("PendingSchedules/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot){
                p0.children.forEach {
                   val date = it.key
                    val ref2 = database.getReference("PendingSchedules/$uid/$date/slot")
                    ref2.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            p0.children.forEach { it ->
                                val slot = it.key?.toInt()
                                val ref3 = database.getReference("PendingSchedules/$uid/$date/slot/$slot")
                                ref3.addListenerForSingleValueEvent(object :ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {
                                        p0.children.forEach {
                                            val pendingSchedule = it.getValue(ClientTransaction::class.java)
                                           pendingRVadapter.add(PendingScheduleRow(ScheduleSlot(pendingSchedule!!
                                               ,getStartTime(slot!!),getEndTime(slot))
                                               ,"$date"))
                                            confirmedRVAdapter.add(ConfirmerScheduleRow(ScheduleSlot(pendingSchedule
                                                ,getStartTime(slot),getEndTime(slot))
                                                ,"$date"))}}
                                    override fun onCancelled(p0: DatabaseError){}})}}
                        override fun onCancelled(p0: DatabaseError){}})}}
            override fun onCancelled(p0: DatabaseError){}})}

private fun getStartTime(slotNumber:Int):String{
    when(slotNumber){
        0 -> return "8:AM"
        1 -> return "9:AM"
        2 -> return "10:AM"
        3 -> return "11:AM"
        4 -> return "12:NN"
        5 -> return "1:PM"
        6 -> return "2:PM"
        7 -> return "3:PM"
        8 -> return "4:PM"
        9 -> return "5:PM"
        10 -> return "6:PM"
        11-> return "7:PM"
        12 -> return "8:PM"
        13 -> return "9:PM"
    }
    return ""
}
    private fun getEndTime(slotNumber:Int):String{
        when(slotNumber){
            0 -> return "9:AM"
            1 -> return "10:AM"
            2 -> return "11:AM"
            3 -> return "12:NM"
            4 -> return "1:NN"
            5 -> return "2:PM"
            6 -> return "3:PM"
            7 -> return "4:PM"
            8 -> return "5:PM"
            9 -> return "6:PM"
            10 -> return "7:PM"
            11-> return "8:PM"
            12 -> return "9:PM"
            13 -> return "10:PM"
        }
        return ""
    }
}