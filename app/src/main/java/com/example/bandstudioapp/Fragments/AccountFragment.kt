package com.example.bandstudioapp.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog.Builder
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.Model.Band
import com.example.bandstudioapp.Model.BandMembers
import com.example.bandstudioapp.Model.Client
import com.example.bandstudioapp.Model.Genre
import com.example.bandstudioapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_account.*
import java.util.*


@SuppressLint("InflateParams")
class AccountFragment : Fragment() {


    private var selectedPhotoUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


        loadAccountDataOnCreate()

        editInfo_accountFragment.setOnClickListener {

            showEditMenu()
        }
        onClickSocialMedia()

    }


//    override fun onResume() {
//        super.onResume()
//        loadAccountData()
//    }


    //shows edit menu/choices for dialog box
    private fun showEditMenu() {

        val builder = Builder(activity)
        builder.setTitle(R.string.edit_menu_itemString)
            .setItems(
                R.array.EditMenuItemsAccountFragment
            ) { dialog, which ->
                when (which) {
                    0 -> changeBandPicture()
                    1 -> updateBandName()
                    2 -> updateLocation()
                    3 -> addBandMembers()
                    4 -> addGenre()
                    5 -> updateDescription()
                    else -> dialog.dismiss()
                }

            }
        builder.create().show()

    }

    //=================Edit Menu Functions ===================//


    private fun addBandMembers() {

        val builder = Builder(context).setTitle("Add Band Members")
        val inflater: View = layoutInflater.inflate(R.layout.spinner_band_members_dialog_box, null)
        val editText = inflater.findViewById<EditText>(R.id.bandMember_name_editText)
        val iSpinnr = inflater.findViewById<Spinner>(R.id.instrument_spinner)
        val adapterInstrument = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.instrument_list)
        )
        adapterInstrument.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        iSpinnr.adapter = adapterInstrument

        builder.setPositiveButton("ADD"
        ) { _, _ ->
            sendBandMemberToFireBase(
                editText.text.toString(),
                iSpinnr.selectedItem.toString()
            )
        }
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog?.dismiss() }
        builder.setView(inflater)
        builder.create().show()

    }

    private fun sendBandMemberToFireBase(name: String, instrument: String) {

        if (instrument == "Instrument" || name == "") {
            Toasty.error(context!!, "Invalid", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = auth.currentUser?.uid
        val ref = database.getReference("bands/$uid/members")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val member = p0.getValue(BandMembers::class.java)
                if (member != null) {
                    if (member.firstName == "" &&
                        member.mainInstrument == ""
                    ) {
                        ref.setValue(BandMembers(name, instrument))
                    } else {
                        ref.setValue(
                            BandMembers(
                                member.firstName + "\n" + name,
                                member.mainInstrument + "\n" + instrument
                            )
                        )
                    }
                }
            }


            override fun onCancelled(p0: DatabaseError) {

            }


        })
        loadAccountData()
    }


    private fun addGenre() {
        val builder = Builder(context).setTitle("Add Genre")
        val inflater: View = layoutInflater.inflate(R.layout.spinner_dialog_box, null)
        val pSpinner = inflater.findViewById<Spinner>(R.id.primary_genre_spinner)
        val mSpinner = inflater.findViewById<Spinner>(R.id.secondary_genre_spinner)
        val adapterPrimary = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.primary_genre_array)
        )
        val adapterSecondary = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.secondary_genre_array)
        )

        adapterPrimary.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterSecondary.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pSpinner.adapter = adapterPrimary
        mSpinner.adapter = adapterSecondary

        builder.setPositiveButton("ADD", object : DialogInterface.OnClickListener {

            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (pSpinner.selectedItem.toString() == "Primary"
                    && mSpinner.selectedItem.toString() == "Secondary"
                ) {
                    Toasty.error(
                        context!!
                        , "No Genre is specified", Toast.LENGTH_SHORT, true
                    ).show()
                    return
                }
                if (pSpinner.selectedItem.toString() == "Primary") {
                    sendGenreToFireBase(
                        mSpinner.selectedItem.toString(),
                        ""
                    )
                    return
                }
                if (mSpinner.selectedItem.toString() == "Secondary") {
                    sendGenreToFireBase(
                        pSpinner.selectedItem.toString(),
                        ""
                    )
                    return
                }
                sendGenreToFireBase(
                    pSpinner.selectedItem.toString(),
                    mSpinner.selectedItem.toString()
                )


            }
        })
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog?.dismiss() }
        builder.setView(inflater)
        builder.create().show()


    }

    private fun sendGenreToFireBase(primaryGenre: String, secondaryGenre: String) {

        val uid = auth.currentUser?.uid
        database.getReference("bands/$uid/genre")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val genre = p0.getValue(Genre::class.java)
                    if (genre != null) {
                        if (genre.subgenre == "") {

                            database.getReference("bands/$uid/genre").child("subgenre")
                                .setValue("$primaryGenre $secondaryGenre")
                        } else {
                            database.getReference("bands/$uid/genre").child("subgenre")
                                .setValue(
                                    genre.subgenre + "| " +
                                            primaryGenre + " " + secondaryGenre
                                )
                        }
                    }
                }


                override fun onCancelled(p0: DatabaseError) {

                }


            })
        loadAccountData()
    }

    private fun updateDescription() {

        val builder = Builder(context).setTitle("Add Description")
        val input = EditText(context)
        input.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        showCurrentMessage(input)

        builder.setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newDescription = input.text.toString().trim()
                val uid = auth.currentUser?.uid ?: ""
                database.getReference("/bands/$uid/description").setValue(newDescription)
                loadAccountData()
            }
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog?.cancel() }

        builder.create().show()


    }

    private fun showCurrentMessage(input: EditText) {

        val uid = auth.currentUser?.uid ?: ""
        database.getReference("/bands/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    val band = p0.getValue(Band::class.java)
                    if (band != null) {
                        input.setText(band.description)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }

    private fun updateLocation() {

        val intent = Intent(context, MapFragment::class.java)
        startActivity(intent)
    }

    private fun updateBandName() {

        val builder = Builder(activity).setTitle("Enter Band Name")
        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)


        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {

                val newBandName = input.text.toString().trim()

                if (newBandName.length > 30) {
                    Toasty.error(
                        context!!, "Must be less than 30 Characters",
                        Toast.LENGTH_SHORT, true
                    ).show()
                    return
                }

                val uid = auth.currentUser?.uid ?: ""
                val ref = database.getReference("/clients/$uid")
                ref.child("bandName").setValue(newBandName)
                val bandRef = database.getReference("bands/$uid")
                bandRef.child("bandName").setValue(newBandName)
                loadAccountData()


            }
        })
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog?.cancel() }

        builder.create().show()


    }

    //Edit Menu: Will update band picture including image to FireBase
    //Prompt user to select a picture
    private fun changeBandPicture() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)

    }

    var cancelDelete = true
    //Displays the picture on image view if NOT null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            cancelDelete = true
            deletePriorBandPic()
            selectedPhotoUri = data.data
            Picasso.get().load(selectedPhotoUri).centerCrop().fit()
                .into(circleImage_accountFragment)
            uploadImageToFireBaseStorage()
        }
    }

   private fun uploadImageToFireBaseStorage() {
        if (selectedPhotoUri == null) {
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                updateUserToFireBaseDatabase(it.toString())
            }
        }
    }


    private fun updateUserToFireBaseDatabase(profileImageUrl: String) {
        val uid = auth.currentUser?.uid ?: ""
        val ref = database.getReference("/clients/$uid")

        ref.child("profileImageUrl").setValue(profileImageUrl)
        cancelDelete = false
    }

    private fun deletePriorBandPic() {
        val currentUser = auth.uid
        val ref = database.getReference("/clients/$currentUser")
        ref.addValueEventListener(object : ValueEventListener {

            //will trigger if there's data or a change of data
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val client =
                    dataSnapshot.getValue(
                        Client::class.java
                    )
                if (client != null) {
                    val imageUrl = client.profileImageUrl
                    var fileName = imageUrl.substring(imageUrl.indexOf("2F") + 2)
                    fileName = fileName.substring(0, fileName.indexOf("?alt"))

                    if (cancelDelete) {
                        storage.reference.child("images/$fileName").delete()
                            .addOnCompleteListener {
                                Toasty.success(
                                    context!!, "Band Image Updated"
                                    , Toast.LENGTH_SHORT, true
                                ).show()
                            }
                    }


                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadAccountData()
    }

    fun loadAccountData() {

        val currentUser = auth.uid
        val ref = database.getReference("/clients/$currentUser")


        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val account = p0.getValue(Client::class.java) ?: return

                Picasso.get().load(account.profileImageUrl).centerCrop().fit()
                    .into(circleImage_accountFragment)
                bandName_viewSchedule.text = account.bandName


                val bandRef = database.getReference("/bands/${account.uid}")
                bandRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val band = p0.getValue(Band::class.java)
                        if (band != null) {
                            location_textView_accountFragment.text = band.location
                            genre_textView_accountFragment.text = band.genre.subgenre
                            story_textView_accountFragment.text = band.description
                            members_textView_accountFragment.text = band.members.firstName
                            instrument_textView_accountFragment.text =
                                band.members.mainInstrument
                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    private fun loadAccountDataOnCreate() {

        val currentUser = auth.uid
        val ref = database.getReference("/clients/$currentUser")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val account = p0.getValue(Client::class.java) ?: return

                Picasso.get().load(account.profileImageUrl).centerCrop().fit()
                    .into(circleImage_accountFragment)


                bandName_viewSchedule.text = account.bandName

                val bandRef = database.getReference("/bands/${account.uid}")
                bandRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val band = p0.getValue(Band::class.java)
                        if (band != null) {
                            location_textView_accountFragment.text = band.location
                            genre_textView_accountFragment.text = band.genre.subgenre
                            story_textView_accountFragment.text = band.description
                            members_textView_accountFragment.text = band.members.firstName
                            instrument_textView_accountFragment.text =
                                band.members.mainInstrument
                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    private fun onClickSocialMedia() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE


        spotifyLink_accountFragment.setOnClickListener {
            sendLinkToFireBase(input, "Spotify Link(Https:/", "Spotify")

        }
        facebookLink_accountFragment.setOnClickListener {
            sendLinkToFireBase(input, "FB Page ID", "Facebook")

        }
        youtubeLink_accountFragment.setOnClickListener {
            sendLinkToFireBase(input, "Youtube Channel Link(Https:/", "Youtube")
        }


    }

    private fun sendLinkToFireBase(editText: EditText, hint: String, socialMediaSelected: String) {

        val builder = Builder(context)
        editText.hint = hint

        if (editText.parent != null) {
            val parent: ViewGroup = editText.parent as ViewGroup
            parent.removeView(editText)
            editText.text.clear()
        }


        builder.setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val linkEntered = editText.text.toString().trim()
                val uid = auth.currentUser?.uid ?: ""
                database.getReference("SocialMedia/$uid/$socialMediaSelected")
                    .setValue(linkEntered)
            }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog?.cancel() }

        builder.create().show()


    }


}







