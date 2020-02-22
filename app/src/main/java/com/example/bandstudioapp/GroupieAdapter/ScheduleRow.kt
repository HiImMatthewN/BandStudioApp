package com.example.bandstudioapp.GroupieAdapter

import android.graphics.Color
import com.example.bandstudioapp.Model.ScheduleSlot
import com.example.bandstudioapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.schedule_row.view.*

class ScheduleRow(val scheduleSlotList: ScheduleSlot, val selectedDate: String, val uid: String) :
    Item<GroupieViewHolder>() {


    override fun getLayout(): Int {
        return R.layout.schedule_row
    }


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val imageUrl = scheduleSlotList.band.profileImage
//        val target = viewHolder.itemView.bandPic_scheduleRow


        viewHolder.itemView.startTime_scheduleFragment.text = scheduleSlotList.startTime
        viewHolder.itemView.endTime_scheduleFragment.text = scheduleSlotList.endTime
        viewHolder.itemView.date_textView_scheduleRow.text = selectedDate
        viewHolder.itemView.bandName_textView_scheduleRow.text = scheduleSlotList.band.bandName


        if (scheduleSlotList.band.state == "OK") {
            viewHolder.itemView.backgroundItem_scheduleRow.setBackgroundColor(Color.parseColor("#FF0000"))
        }

        else if  (scheduleSlotList.band.state == "Pending") {
            viewHolder.itemView.backgroundItem_scheduleRow.setBackgroundColor(Color.parseColor("#FDB813"))
        }
         if (scheduleSlotList.band.isAnonymous && scheduleSlotList.band.uid != uid) {
            viewHolder.itemView.backgroundItem_scheduleRow.setBackgroundColor(Color.parseColor("#A4A4A4"))
            viewHolder.itemView.bandName_textView_scheduleRow.text = "Anonymous"
        }
        else if(scheduleSlotList.band.bandName == "N/A"){
            viewHolder.itemView.backgroundItem_scheduleRow.setBackgroundColor(Color.parseColor("#1DB954"))
        }

    }

}