package com.example.bandstudioapp.Admin


import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bandstudioapp.R
import com.example.bandstudioapp.databinding.FragmentAdminBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.fragment_admin.*
import org.json.JSONException
import org.json.JSONObject

class AdminFragment : Fragment() {

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" +
            "AAAAHBxNCXo:APA91bGvI7pzBveKXeFXyuj_UyNWAqUz0y_Pxk83QePy1Yii2OW47XSYp5BBsB-d45AA35" +
            "_nVbX6f1kpUbDRfCXrqMw0TIPYWlDhEwZ1aQzWOHwz_ltYqnNj9Nv5NJJZOxcBuRYiERlo"
    private val contentType = "application/json"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val binding = DataBindingUtil.setContentView<FragmentAdminBinding>(
            activity!!,
            R.layout.fragment_admin
        )
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/Bookings")

        binding.sendNotificationButton.setOnClickListener {
            if (!TextUtils.isEmpty(binding.bodyNotification.text)) {

                val topic = "/topics/Bookings"
                val notification = JSONObject()
                val notificationBody = JSONObject()

                try {

                    notificationBody.put("title", "Notification")
                    notificationBody.put("message", binding.bodyNotification.text)
                    notification.put("to", topic)
                    notification.put("data", notificationBody)
                } catch (e: JSONException) {
                    //message
                }
                sendNotification(notification)

            }
        }


    }

    private fun sendNotification(notification:JSONObject) {
        val jsonObjectRequest = object :JsonObjectRequest(FCM_API,notification, Response.Listener<JSONObject> {
            response ->
            body_notification.setText("")
        },Response.ErrorListener {
            Toast.makeText(context,"Request Error",Toast.LENGTH_SHORT).show()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val params =HashMap<String,String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)


    }
}