package com.example.bandstudioapp

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        val CHANNEL_ID = "texas_studio"
        val CHANNEL_NAME = "Texas Studio"
        val CHANNEL_DESCRIPTION = "Texas Studio Notifications"
    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(p0: RemoteMessage) {
       if( isAppRunning(this,"com.example.bandstudioapp")){

            val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                setUpChannel(notificationManager)

            }

            val title = p0.data.get("title")
            val text = p0.data.get("message")
            NotificationHelper.displayNotification(applicationContext, title!!, text!!)

        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpChannel(notificationManager: NotificationManager) {


        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME
            , NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = CHANNEL_DESCRIPTION
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    private fun isAppRunning(context:Context, packageName:String):Boolean{

        val activityManager =context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos =activityManager.runningAppProcesses
        if(procInfos !=null){
            for(processInfo in procInfos){
                if(processInfo.processName == packageName){
                    return true
                }
            }
        }

        return false
    }



}