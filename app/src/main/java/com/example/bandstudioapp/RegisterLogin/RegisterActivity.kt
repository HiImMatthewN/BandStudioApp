package com.example.bandstudioapp.RegisterLogin

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bandstudioapp.R
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        register_button_register.setOnClickListener {

            registerUser()
        }
    }


    //register user to firebase
    private fun registerUser() {
        checkInputValidation()
    }


    //Check if edit text in register activity is valid
    private fun checkInputValidation() {
        val emailEntered = email_editText_register.text.toString().trim()
        val passwordEntered = confirmPassword_editText_register.text.toString().trim()
        val confirmedPassword = password_editText_register.text.toString().trim()

        val errorMessage = StringBuilder()
        var errorCount = 0


        if (emailEntered.isEmpty()) {
            errorMessage.append("Email Field is empty")
            errorCount++
        }
        if (passwordEntered.isEmpty()) {
            if (errorCount > 0) {
                errorMessage.append("\n")
            }
            errorMessage.append("Password Field is empty")
        }
        if (passwordEntered != (confirmedPassword)) {
            if (errorCount > 0) {
                errorMessage.append("\n")
            }
            errorMessage.append("Password Mismatch")
        }
        if (errorMessage.isNotEmpty()) {
//            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show()
            Toasty.error(this, errorMessage, Toast.LENGTH_SHORT, true).show()
            return
        } else {

            auth.createUserWithEmailAndPassword(emailEntered, passwordEntered)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                    } else {
                        Toasty.success(this, "Registration Success", Toast.LENGTH_SHORT, true)
                            .show()

                        //Starts RegisterBandProfileActivity
                        val intent = Intent(this, RegisterBandProfileActivity::class.java)
                        startActivity(intent)
                        this.overridePendingTransition(
                            R.anim.slide_in_left,
                            R.anim.slide_out_left
                        )
                        finish()
                    }
                }.addOnFailureListener {
                    Toasty.error(
                        this,
                        "Registration Failed ${it.message}",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                    return@addOnFailureListener
                }


        }
    }


}
