package com.example.bandstudioapp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)


            val title = p0.notification?.title
            val text = p0.notification?.body
            NotificationHelper.displayNotification(MyClass.getContext(),title!!,text!!)



    }
}