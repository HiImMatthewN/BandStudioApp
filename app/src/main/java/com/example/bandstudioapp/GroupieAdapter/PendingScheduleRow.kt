package com.example.bandstudioapp.GroupieAdapter

import android.graphics.Color
import com.example.bandstudioapp.Model.ScheduleSlot
import com.example.bandstudioapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.schedule_row.view.*

class PendingScheduleRow(val scheduleSlotList: ScheduleSlot, val selectedDate: String):
    Item<GroupieViewHolder>() {


    override fun getLayout(): Int {
        return R.layout.schedule_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.startTime_scheduleFragment.text = scheduleSlotList.startTime
        viewHolder.itemView.endTime_scheduleFragment.text = scheduleSlotList.endTime
        viewHolder.itemView.date_textView_scheduleRow.text = selectedDate
        viewHolder.itemView.bandName_textView_scheduleRow.text = scheduleSlotList.band.bandName
        viewHolder.itemView.backgroundItem_scheduleRow.setBackgroundColor(Color.parseColor("#FDB813"))


    }
}