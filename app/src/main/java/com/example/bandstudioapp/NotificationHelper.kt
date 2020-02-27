package com.example.bandstudioapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bandstudioapp.Main.DashboardActivity
import java.util.*

abstract class NotificationHelper {

    companion object{
        fun displayNotification(context: Context,title:String,body:String) {
            val intent = Intent(context,DashboardActivity::class.java)
            val notificationID = Random().nextInt(3000)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(context,100,intent
                ,PendingIntent.FLAG_ONE_SHOT)



            val mBuilder = NotificationCompat.Builder(context, FirebaseMessagingService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_fcm).setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(notificationID, mBuilder.build())


        }
    }

}