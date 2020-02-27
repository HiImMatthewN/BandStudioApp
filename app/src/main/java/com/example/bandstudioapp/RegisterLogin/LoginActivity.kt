package com.example.bandstudioapp.RegisterLogin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bandstudioapp.Admin.AdminActivity
import com.example.bandstudioapp.IntroScreen.IntroActivity
import com.example.bandstudioapp.MyClass
import com.example.bandstudioapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyClass.setContext(this)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        setContentView(R.layout.activity_login)
        typingAnimation(welcome_textView_login, "Welcome User!", 1)
        progressBar = findViewById(R.id.progressBar_login)



        showHidePasswordListener()


        register_textView_login.setOnClickListener {
            loadRegisterActivity()
        }

        login_button_login.setOnClickListener {

            login()
            showProgressDialog(true)
        }
    }

    private fun login() {
        val email = email_login.text.toString().trim()
        val password = password_login.text.toString().trim()


        if (email.isEmpty() || password.isEmpty()) {
            Toasty.error(
                this, "Login Failed"
                , Toast.LENGTH_SHORT, true
            ).show()
            email_login.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnFailureListener {
                Toasty.error(
                    this, "Login Failed ${it.message}"
                    , Toast.LENGTH_SHORT, true
                ).show()

            }.addOnCompleteListener {
                if (!it.isSuccessful) {
                    showProgressDialog(false)
                    return@addOnCompleteListener
                }
                if (auth.uid == "zPaOMtY16oOar51SAZcdJf1Blv82") {
                    val intent = Intent(
                        this,
                        AdminActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                    Toasty.success(
                        this, "Logging as Admin"
                        , Toast.LENGTH_SHORT, true
                    ).show()
                    return@addOnCompleteListener
                }


                val intent = Intent(
                    this,
                    IntroActivity::class.java
                )
                getToken()
                startActivity(intent)
                finish()
            }

    }





    private fun loadRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )

    }


    private fun showHidePasswordListener() {

        showPassword_login.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                password_login.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                password_login.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

    }

    //Shows progressBar and disable user interaction to the app
    private fun showProgressDialog(isShowing: Boolean) {
        if (isShowing) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE

            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }


    }

    private fun typingAnimation(view: TextView, text: String, length: Int) {
        var delay = 200L
        if (Character.isWhitespace(text.elementAt(length - 1))) {
            delay = 600L
        }
        view.text = text.substring(0, length)
        when (length) {
            text.length -> Handler().postDelayed({
                Handler().postDelayed({
                    typingAnimation(view, text, 1)
                }, delay)
            }, 5000)
            else -> Handler().postDelayed({
                typingAnimation(view, text, length + 1)
            }, delay)
        }
    }

    private fun getToken() {


        FirebaseMessaging.getInstance().subscribeToTopic("Bookings")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val token = it.result?.token
                    if (token != null)
                        saveToken(token)
                }
            }
    }

    private fun saveToken(token: String) {
        val uid = auth.currentUser?.uid
        val ref = database.getReference("clients/$uid").child("token")
        ref.setValue(token)

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            if (auth.uid == "zPaOMtY16oOar51SAZcdJf1Blv82") {
                val intent = Intent(
                    this,
                    AdminActivity::class.java
                )
                startActivity(intent)
            } else {
                val intent = Intent(
                    this,
                    IntroActivity::class.java
                )
                startActivity(intent)
                finish()
            }

        }
    }
}