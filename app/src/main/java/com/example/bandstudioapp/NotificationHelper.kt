package com.example.bandstudioapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bandstudioapp.Admin.AdminActivity
import com.example.bandstudioapp.Main.DashboardActivity

abstract class NotificationHelper {

    companion object{
        fun displayNotification(context: Context,title:String,body:String) {
            val intent = Intent(context,DashboardActivity::class.java)


            val pendingIntent = PendingIntent.getActivity(context,100,intent
                ,PendingIntent.FLAG_CANCEL_CURRENT)



            val mBuilder = NotificationCompat.Builder(context, AdminActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_fcm).setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(1, mBuilder.build())


        }
    }

}