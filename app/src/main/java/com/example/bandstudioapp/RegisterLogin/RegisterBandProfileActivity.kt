package com.example.bandstudioapp.RegisterLogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bandstudioapp.Model.Band
import com.example.bandstudioapp.Model.BandMembers
import com.example.bandstudioapp.Model.Client
import com.example.bandstudioapp.Model.Genre
import com.example.bandstudioapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_register_band_profile.*
import java.util.*

class RegisterBandProfileActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private lateinit var bandName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_register_band_profile)




        progressBar = findViewById(R.id.progressBar_register)


        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()


        imageView_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        finish_button_register.setOnClickListener {


            bandName = bandName_editText_register.text.toString().trim()
            uploadBandProfile()
            showProgressDialog()


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Picasso.get().load(selectedPhotoUri).centerCrop().fit().into(imageView_register)
            textViewSelect_register.alpha = 0f


        }
    }

    private fun uploadBandProfile() {

        if (bandName.isNotEmpty()) {
            uploadImageToFirebaseStorage()
        } else {
            Toasty.error(
                this, "Please enter your band name"
                , Toast.LENGTH_SHORT, true
            ).show()

            return

        }

    }

    fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            Toasty.error(
                this, "Select a band picture"
                , Toast.LENGTH_SHORT, true
            ).show()
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserToFirebaseDatabase(it.toString())
            }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = database.getReference("/clients/$uid")


        //Creates an instance of Client
        val client = Client(
            uid,
            bandName,
            profileImageUrl,"", true
        )

        //Sends that instance to the FireBase DB
        ref.setValue(client).addOnSuccessListener {
            uploadBandDetails(client.bandName,client.uid)

        }.addOnFailureListener {
            Toasty.error(this, "${it.message}", Toast.LENGTH_SHORT, true).show()
        }

    }

    //Shows progressBar and disable user interaction to the app
    private fun showProgressDialog() {
        progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

    }


    private fun uploadBandDetails(bandName: String,uid: String) {
        val newBand = Band(bandName, "",
            BandMembers("",""), Genre(""), "")


        val ref = database.getReference("/bands/$uid")
        ref.setValue(newBand).addOnCompleteListener {
            this.overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }


}
