package com.example.bandstudioapp.Main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.Fragments.*
import com.example.bandstudioapp.Model.BugReport
import com.example.bandstudioapp.Model.Client
import com.example.bandstudioapp.Model.Event
import com.example.bandstudioapp.MyClass
import com.example.bandstudioapp.R
import com.example.bandstudioapp.RegisterLogin.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.processphoenix.ProcessPhoenix
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.nav_header.*
import java.util.*


var lastClickTime: Long = 0
var selectedFragment: Fragment? = null
@SuppressLint("InflateParams")
class DashboardActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage:FirebaseStorage
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        if (savedInstanceState == null) {
            showBandNameToast()
        }

        setFullScreen()
        setContentView(R.layout.activity_welcome)
        setNavigationDrawer()
        setBottomNavigation(savedInstanceState)
        loadDataToNavDrawer()




        add_event_item_Button.visibility = View.GONE
        val s = savedInstanceState?.getString("SelectedFragment", "") ?: ""
        if (s != "") {
            if (s.substring(0, 4) == EventFragment().toString().substring(0, 4)) {
                add_event_item_Button.visibility = View.VISIBLE
            }
        }



        add_event_item_Button.setOnClickListener {
            showAddEventDialog()
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SelectedFragment", selectedFragment.toString())

    }


    private var selectedPhotoUri: Uri? = null
    private var posterUrl:String? =null
    private var eventPoster: ImageView? = null

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showAddEventDialog() {

        val inflater = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null)
        val dialog = Dialog(this)


        eventPoster = inflater.findViewById(R.id.add_event_poster)
        val eventNameET = inflater.findViewById<EditText>(R.id.add_event_title)
        val eventVenueET = inflater.findViewById<EditText>(R.id.add_venue_name)
        val nextButton = inflater.findViewById<Button>(R.id.next_info_button)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(inflater)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.window?.setLayout(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.show()



        eventPoster?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


        nextButton.setOnClickListener {
            val eventName = eventNameET.text.toString().trim()
            val eventVenue = eventVenueET.text.toString().trim()

            if ((posterUrl == "")
                || eventName == ""
                || eventVenue == ""
            ) {
                Toasty.error(
                    this,
                    "Missing Fields", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showAddEventDialog2(posterUrl!!, eventName, eventVenue,dialog)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Picasso.get().load(selectedPhotoUri).centerCrop().fit().into(eventPoster)
            uploadImageToFireBaseStorage()
        }
    }
   private fun uploadImageToFireBaseStorage(){

        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference("/eventPosters/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                posterUrl = it.toString()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showAddEventDialog2(poster: String, eventName: String, eventVenue: String,dialogInfo1:Dialog) {
        val inflater = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_event_addtnal_info, null)

        // Initialize
        val eventDateDP = inflater.findViewById<DatePicker>(R.id.add_event_date)
        val eventTimeTP = inflater.findViewById<TimePicker>(R.id.add_event_time)
        val eventPriceET = inflater.findViewById<EditText>(R.id.add_event_price)
        val productionNameET = inflater.findViewById<EditText>(R.id.add_production_name)
        val addButton = inflater.findViewById<Button>(R.id.save_event_button)



        val dialogInfo2 = Dialog(this)
        dialogInfo2.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogInfo2.setContentView(inflater)
        dialogInfo2.setCanceledOnTouchOutside(true)
        dialogInfo2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogInfo2.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialogInfo2.window?.setLayout(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialogInfo2.show()


        addButton.setOnClickListener {
            // Get Value
            val eventDate = "${eventDateDP.month} ${eventDateDP.dayOfMonth} " +
                    "${eventDateDP.year}"
            var eventTimeHour = eventTimeTP.hour
            val eventTimeMin = eventTimeTP.minute
            var timeType = "AM"
            if(eventTimeHour > 12){
                timeType = "PM"
                eventTimeHour -= 12
            }else if(eventTimeHour == 12)
                timeType = "NN"


            val eventPrice = eventPriceET.text.toString().trim().toInt()
            val productionName = productionNameET.text.toString().trim()

            if (eventDate == ""
                || eventTimeHour.toString() == ""
                || eventPrice.toString() == ""
                || productionName == ""
            ) {
                Toasty.error(
                    this,
                    "Missing Fields"
                    , Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            saveEventToFirebase(Event(poster,eventName,
                eventVenue,eventDate,eventPrice,productionName,
                "$eventTimeHour:$eventTimeMin$timeType"),eventName)
            dialogInfo1.dismiss()
            dialogInfo2.dismiss()
            Toasty.success(this,"Event $eventName added!"
                ,Toast.LENGTH_SHORT).show()
        }


    }

    private fun saveEventToFirebase(newEvent: Event,eventName:String) {

        database.getReference("Events/$eventName")
            .setValue(newEvent)


    }


    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =

        object : BottomNavigationView.OnNavigationItemSelectedListener {

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (SystemClock.elapsedRealtime() - lastClickTime < 750) {
                    return false
                }
                lastClickTime = SystemClock.elapsedRealtime()

                when (item.itemId) {
                    R.id.nav_home -> {
                        selectedFragment =
                            HomeFragment()
                        add_event_item_Button.visibility = View.GONE
                    }
                    R.id.nav_book -> {
                        selectedFragment =
                            BookFragment()
                        add_event_item_Button.visibility = View.GONE
                    }
                    R.id.nav_events -> {

                        selectedFragment =
                            EventFragment()
                        add_event_item_Button.visibility = View.VISIBLE
                    }
                    R.id.nav_account -> {
                        selectedFragment =
                            AccountFragment()
                        add_event_item_Button.visibility = View.GONE
                    }
                }
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    selectedFragment!!
                ).commit()
                return true

            }
        }


    private fun setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


    }

    private fun setNavigationDrawer() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        drawer = findViewById(R.id.draw_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(navDrawer)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()


    }

    private val navDrawer: NavigationView.OnNavigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_log_out -> {
                    showLogOutDialog()
                }
                R.id.nav_bug_report -> {
                    showBugReportDialog()
                }
                R.id.nav_location -> {
                    val intent = Intent(MyClass.getContext(), GetDirectionFragment::class.java)
                    startActivity(intent)
                }

            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }


    //Closes the navigation drawer (if open) when pressing the back button
    // instead of closing the activity.
    override fun onBackPressed() {

        // Use GravityCompat.Start if the drawer is on the Left Side
        // otherwise use GravityCompat.End
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    private fun setBottomNavigation(savedInstanceState: Bundle?) {

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
    }

    private fun loadDataToNavDrawer() {

        val uid = auth.currentUser?.uid
        val ref = database.getReference("clients/$uid")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val client = p0.getValue(Client::class.java) ?: return
                try {
                    Picasso.get().load(client.profileImageUrl).centerCrop().fit()
                        .into(navHeader_circleImageView)
                    navHeader_bandName.text = client.bandName
                    navHeader_email.text = auth.currentUser?.email
                } catch (e: IllegalArgumentException) {
                    showBandNameToast()
                    val intent = Intent(this@DashboardActivity, DashboardActivity::class.java)
                    ProcessPhoenix.triggerRebirth(this@DashboardActivity, intent)
                }


            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun showBandNameToast() {
        val uid = auth.currentUser?.uid
        val ref = database.getReference("clients/$uid/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Client::class.java) ?: return
                Toasty.success(
                    this@DashboardActivity,
                    "Logging as ${user.bandName}", Toast.LENGTH_SHORT
                ).show()

            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }

    //===================Navigation Drawer Menu ======================//
    private fun showLogOutDialog() {

        val builder = AlertDialog.Builder(this@DashboardActivity)
        builder.setTitle("Logging out...  Continue?")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(
                    this@DashboardActivity
                    , LoginActivity::class.java
                )
                auth.signOut()
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel"
            ) { dialog, _ -> dialog?.dismiss() }.create().show()

    }

    private fun showBugReportDialog() {

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater.inflate(R.layout.dialog_bug_report, null)
        val subjectEditTxt: EditText = inflater.findViewById(R.id.subject_editText_bugReport)
        val androidOSET: EditText = inflater.findViewById(R.id.androidOS_editText_bugReport)
        val phoneModelET:EditText = inflater.findViewById(R.id.phoneModel_editText_bugReport)
        val descriptionEditTxt: EditText =
            inflater.findViewById(R.id.description_editText_bugReport)

        builder.setPositiveButton("Send"
        ) { _, _ ->
            val subjectText = subjectEditTxt.text.toString().trim()
            val descriptionText = descriptionEditTxt.text.toString().trim()
            val androidOSText = androidOSET.text.toString().trim()
            val phoneModelText = phoneModelET.text.toString().trim()


            val uid = auth.currentUser?.uid
            val ref = database.getReference("BugReports/$uid/$subjectText")
            val bugReport = BugReport(subjectText,androidOSText,phoneModelText, descriptionText)
            ref.setValue(bugReport).addOnCompleteListener {
                Toasty.success(this,
                    "Bug report sent.",Toast.LENGTH_LONG).show()
            }
        }
            .setNegativeButton("Cancel"
            ) { dialog, _ -> dialog?.dismiss() }
            .setView(inflater).create().show()

    }

}
