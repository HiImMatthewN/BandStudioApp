package com.example.bandstudioapp.Admin


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bandstudioapp.Fragments.monthFormat
import com.example.bandstudioapp.Model.ClientTransaction
import com.example.bandstudioapp.Model.Schedule
import com.example.bandstudioapp.Model.ScheduleSlot
import com.example.bandstudioapp.R
import com.example.bandstudioapp.RegisterLogin.LoginActivity
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_admin.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {
    private lateinit var drawer: DrawerLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var datePicker: CompactCalendarView
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" +
            "AAAAHBxNCXo:APA91bGvI7pzBveKXeFXyuj_UyNWAqUz0y_Pxk83QePy1Yii2OW47XSYp5BBsB-d45AA35" +
            "_nVbX6f1kpUbDRfCXrqMw0TIPYWlDhEwZ1aQzWOHwz_ltYqnNj9Nv5NJJZOxcBuRYiERlo"
    private val contentType = "application/json"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()





        setFullScreen()
        setContentView(R.layout.activity_admin)
        setNavigationDrawer(savedInstanceState)
//        initializeBind()
        initiliazeCount()

        send_notification_button.setOnClickListener {

            if(body_notification.text.isNotEmpty() && subject_notification.text.isNotEmpty()){
                val notifmsg = body_notification.text.toString().trim()
                val bodymsg = subject_notification.text.toString().trim()
                setUpMessage(notifmsg,bodymsg)


            }




        }


    }




    private fun initiliazeCount() {
        val navigationView: NavigationView = findViewById(R.id.nav_view_admin)
        val pendingBooks =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_pendingBooks_admin)) as TextView
        val pendingBooksRef = database.getReference("PendingSchedules/")
        pendingBooksRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                val count = p0.childrenCount
                if (count < 1)
                    pendingBooks.visibility = View.INVISIBLE
                else
                    pendingBooks.text = count.toString()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
        pendingBooks.gravity = Gravity.CENTER_VERTICAL
        pendingBooks.setTypeface(null, Typeface.BOLD)
        pendingBooks.setTextColor(Color.parseColor("#FF0000"))
        val bugReport =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_bug_report_admin)) as TextView
        val bugReportRef = database.getReference("BugReports/")
        bugReportRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val count = p0.childrenCount
                if (count < 1)
                    bugReport.visibility = View.INVISIBLE
                else
                    bugReport.text = count.toString()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
        bugReport.gravity = Gravity.CENTER_VERTICAL
        bugReport.setTypeface(null, Typeface.BOLD)
        bugReport.setTextColor(Color.parseColor("#FF0000"))

    }


    private fun setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


    }

    private fun setNavigationDrawer(savedInstanceState: Bundle?) {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.admin_toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        drawer = findViewById(R.id.draw_layout_admin)
        val navigationView = findViewById<NavigationView>(R.id.nav_view_admin)
        navigationView.setNavigationItemSelectedListener(navDrawer)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

//        if(savedInstanceState == null){
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container_nav_admin,AdminFragment()).commit()
//        }

    }

    private val navDrawer: NavigationView.OnNavigationItemSelectedListener =
        object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.nav_log_out -> {
                        showLogOutDialog()
                    }
                    R.id.nav_bug_report -> {

                    }
                    R.id.nav_addSchedule_admin ->
                        showDatePicker()
                }
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        }

    override fun onBackPressed() {

        // Use GravityCompat.Start if the drawer is on the Left Side
        // otherwise use GravityCompat.End
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    private fun showDatePicker() {

        var monthTitle: String?
        showDialog()
        fetchAvailableDates()


        datePicker.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                addsSchedule(dateClicked)

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                monthTitle = monthFormat.format(firstDayOfNewMonth)
                Toast.makeText(
                    this@AdminActivity, monthTitle
                    , Toast.LENGTH_SHORT
                ).show()

            }
        })


    }
    private fun setUpMessage(notifMessage:String,notifBody:String){


        FirebaseMessaging.getInstance().subscribeToTopic("/topics/Bookings")




                val topic = "/topics/Bookings"
                val notification = JSONObject()
                val notificationBody = JSONObject()

                try {

                    notificationBody.put("title", notifBody)
                    notificationBody.put("message", notifMessage)
                    notification.put("to", topic)
                    notification.put("data", notificationBody)
                } catch (e: JSONException) {
                    //message
                }
                sendNotification(notification)




    }
    private fun sendNotification(notification:JSONObject) {
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API,notification, Response.Listener<JSONObject> {
                response ->
            body_notification.setText("")
        }, Response.ErrorListener {
            Toast.makeText(this,"Request Error",Toast.LENGTH_SHORT).show()
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


    private fun showDialog() {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.dialog_date, null)

        datePicker = v.findViewById(R.id.dialog_date_picker)
        datePicker.setFirstDayOfWeek(Calendar.MONDAY)
        AlertDialog.Builder(this)
            .setView(v)
            .setTitle("Choose Date")
            .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            }
            )
            .create().show()

    }

    //fetches Available Scheds
    private fun fetchAvailableDates() {

        val ref1 = database.getReference("Schedules/")
        ref1.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val epouchValue = it.getValue(Schedule::class.java)
                    datePicker.addEvent(Event(Color.GREEN, epouchValue!!.epouchValue))
                }


            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


    }

    // add Schedule to date picker
    private fun addsSchedule(dateClicked: Date) {
        datePicker.addEvent(Event(Color.GREEN, dateClicked.time, "Available Schedule"))

//    val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
        val format = SimpleDateFormat("MMM dd EEE yyyy")
        val date = format.format(dateClicked)
        val ref = database.getReference("Schedules/$date")
        val schedList = mutableListOf<ScheduleSlot>()

        for (i in 0 until 14) {
            //Initial Time
            var startTime = 8 + i
            var endTime = startTime + 1
            //Start Time
            var startTimeType = ":AM"
            if (startTime == 12) startTimeType = ":NN"
            if (startTime > 12) {
                startTimeType = ":PM"
                startTime -= 12
            }
            //End Time
            var endTimeType = ":AM"
            if (endTime == 12) endTimeType = ":NN"
            if (endTime > 12) {
                endTimeType = ":PM"
                endTime -= 12
            }

            schedList.add(
                ScheduleSlot(
                    ClientTransaction("", "N/A", "", "", false)
                    , startTime.toString() + startTimeType, endTime.toString() + endTimeType
                )
            )
        }

        ref.setValue(Schedule(dateClicked.time, schedList)).addOnCompleteListener {
            if (it.isSuccessful) {
                Toasty.success(
                    this@AdminActivity, "Schedule $date Added",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@AdminActivity, "Schedule $date not added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showLogOutDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logging out...  Continue?")
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(
                        this@AdminActivity
                        , LoginActivity::class.java
                    )

                    auth.signOut()
                    startActivity(intent)
                    finish()
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            }).create().show()

    }
}
