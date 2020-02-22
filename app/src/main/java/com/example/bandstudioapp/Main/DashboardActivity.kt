package com.example.bandstudioapp.Main

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.Fragments.*
import com.example.bandstudioapp.Model.BugReport
import com.example.bandstudioapp.Model.Client
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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header.*


var lastClickTime: Long = 0


class DashboardActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setFullScreen()
        setContentView(R.layout.activity_welcome)
        setNavigationDrawer()
        setBottomNavigation(savedInstanceState)
        loadDataToNavDrawer()


    }


    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =

        object : BottomNavigationView.OnNavigationItemSelectedListener {

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (SystemClock.elapsedRealtime() - lastClickTime < 750) {
                    return false
                }
                lastClickTime = SystemClock.elapsedRealtime()
                var selectedFragment: Fragment? = null
                when (item.getItemId()) {
                    R.id.nav_home -> selectedFragment =
                        HomeFragment()
                    R.id.nav_book -> selectedFragment =
                        BookFragment()
                    R.id.nav_events -> selectedFragment =
                        EventFragment()
                    R.id.nav_account -> selectedFragment =
                        AccountFragment()
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
        object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.nav_log_out -> {
                        showLogOutDialog()
                    }
                    R.id.nav_bug_report -> {
                        showBugReportDialog()
                    }
                    R.id.nav_location ->{
                        val intent = Intent(MyClass.getContext(), GetDirectionFragment::class.java)
                        startActivity(intent)
                    }

                }
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
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
            }catch (e:IllegalArgumentException){
              //message
            }
//                navHeader_textView.text = client.bandName

            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    //===================Navigation Drawer Menu ======================//
    private fun showLogOutDialog() {

        val builder = AlertDialog.Builder(this@DashboardActivity)
        builder.setTitle("Logging out...  Continue?")
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(
                        this@DashboardActivity
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

    private fun showBugReportDialog(){

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater.inflate(R.layout.dialog_bug_report,null)
        val subjectEditTxt: EditText = inflater.findViewById(R.id.subject_editText_bugReport)
        val descriptionEditTxt: EditText = inflater.findViewById(R.id.description_editText_bugReport)

        builder.setPositiveButton("Send",object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val subjectText = subjectEditTxt.text.toString()
                val descriptionText = descriptionEditTxt.text.toString()
                val uid = auth.currentUser?.uid
                val ref = database.getReference("BugReports/$uid").push()
                val bugReport = BugReport(subjectText,descriptionText)
                ref.setValue(bugReport)

            }
        })
            .setNegativeButton("Cancel", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
            .setView(inflater).create().show()

    }

}
