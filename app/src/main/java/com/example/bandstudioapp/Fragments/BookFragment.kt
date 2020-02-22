package com.example.bandstudioapp.Fragments

import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bandstudioapp.GroupieAdapter.ScheduleRow
import com.example.bandstudioapp.Misc.SwipeController
import com.example.bandstudioapp.Misc.SwipeControllerActions
import com.example.bandstudioapp.Model.Client
import com.example.bandstudioapp.Model.ClientTransaction
import com.example.bandstudioapp.Model.Schedule
import com.example.bandstudioapp.Model.ScheduleSlot
import com.example.bandstudioapp.R
import com.example.bandstudioapp.SocialMediaLinkage
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_book.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


val monthFormat = SimpleDateFormat("MMMM ", Locale.getDefault())
private lateinit var database: FirebaseDatabase
private lateinit var auth: FirebaseAuth
private lateinit var datePicker: CompactCalendarView

var queryType: String? = null
var isAnonymous = false
var lastItemSelected:Int? = null


class BookFragment : Fragment() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        queryType = "NONE"

        img_anonymous.visibility = View.GONE

        recyclerView_fragmentBook.adapter = adapter

        fetchTodaySchedule()
        initSwipeMenuController()
        showCurrentDateTime()
        showBookAsAnonymousDialog()

        book_schedule_FragmentBook.setOnClickListener {
            showSearchMenuDialog()

        }


    }

    private fun initSwipeMenuController() {

        val swipeController =
            SwipeController(object :
                SwipeControllerActions() {

                //Book
                override fun onRightClicked(position: Int) {
                    val selectedSchedule = adapter.getItem(position) as ScheduleRow
                    if (verifyAvailability(selectedSchedule)) {
                        lastItemSelected = position
                        bookSchedule(selectedSchedule)
                        showSnackBar()
                    }
                }

                // Swipe Menu Edit
                override fun onLeftClicked(position: Int) {
                    val selectedSchedule = adapter.getItem(position) as ScheduleRow
                    if (selectedSchedule.scheduleSlotList.band.bandName == "N/A") {
                        Toasty.error(context!!, "Empty Slot", Toast.LENGTH_SHORT).show()
                        return
                    }
                    viewSelectedSchedule2(selectedSchedule)
                }
            })

        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(recyclerView_fragmentBook)

        recyclerView_fragmentBook.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView) {
                swipeController.onDraw(c)
            }
        })

    }
    private fun bookSchedule(selectedSlot: ScheduleRow) {

        val uid = auth.currentUser?.uid
        val date = selectedSlot.selectedDate
        val startTime = selectedSlot.scheduleSlotList.startTime
        val formmatedTime: String?
        var slot: Int? = null

        if (startTime.length == 5) {

            formmatedTime = startTime.substring(0, 2)
            if (!startTime.contains('P')) {
                slot = formmatedTime.toInt() - 8
            } else
                slot = formmatedTime.toInt() + 4

        } else if (startTime.length == 4) {

            formmatedTime = startTime.substring(0, 1)
            if (!startTime.contains('P')) {
                slot = formmatedTime.toInt() - 8
            } else
                slot = formmatedTime.toInt() + 4

        }


        val ref = database.getReference("clients/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Client::class.java) ?: return
                val pendingRef =
                    database.getReference("PendingSchedules/${user.uid}/$date/slot/$slot")
                val scheduleSlotRef = database.getReference("Schedules/$date/slot/$slot")
                if (isAnonymous) {
                    pendingRef.child("band")
                        .setValue(
                            ClientTransaction
                                (
                                user.uid, user.bandName, user.profileImageUrl
                                , "Pending", true
                            )
                        )
                    scheduleSlotRef.child("band").setValue(
                        ClientTransaction(
                            user.uid, user.bandName, user.profileImageUrl
                            , "Pending", true
                        )
                    )
                } else {
                    pendingRef.child("band").setValue(
                        ClientTransaction(
                            user.uid, user.bandName, user.profileImageUrl
                            , "Pending", false
                        )
                    )

                    scheduleSlotRef.child("band").setValue(
                        ClientTransaction(
                            user.uid, user.bandName, user.profileImageUrl
                            , "Pending", false
                        )
                    )

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        if (queryType == "DOW") {
            val formmatedDate = date.substring(0, 3)
            var newDate: String? = null
            when (formmatedDate) {
                "Mon" -> newDate = "Monday"
                "Tue" -> newDate = "Tuesday"
                "Wed" -> newDate = "Wednesday"
                "Thu" -> newDate = "Thursday"
                "Fri" -> newDate = "Friday"
                "Sat" -> newDate = "Saturday"
                "Sun" -> newDate = "Sunday"
            }
            queryBySelectedDow(newDate!!)
        } else if (queryType == "TIME") {
            val startItem = adapter.getItem(0) as ScheduleRow
            val endItem = adapter.getItem(adapter.itemCount - 1) as ScheduleRow
            val startTimeAdapter = startItem.scheduleSlotList.startTime
            val endTimeAdapter = endItem.scheduleSlotList.endTime
            var formattedSTimeAdapter: Int
            var formattedETimeAdapter: Int
            if (startTimeAdapter.length == 5) {
                formattedSTimeAdapter = startTimeAdapter.substring(0, 2).toInt()
            } else formattedSTimeAdapter = startTimeAdapter.substring(0, 1).toInt()
            if (endTimeAdapter.length == 5) {
                formattedETimeAdapter = endTimeAdapter.substring(0, 2).toInt()
            } else formattedETimeAdapter = endTimeAdapter.substring(0, 1).toInt()

            if (formattedSTimeAdapter < 8) {
                formattedSTimeAdapter += 12
            }
            if (formattedETimeAdapter < 8) {
                formattedETimeAdapter += 12
            }
            querySchedByTime(formattedSTimeAdapter, formattedETimeAdapter)
        } else {
            fetchSelectedSchedule(selectedSlot.selectedDate)
        }
    }
    private fun verifyAvailability(selectedSlot: ScheduleRow): Boolean {
        val uid = auth.uid
        if (selectedSlot.scheduleSlotList.band.uid != uid
            && selectedSlot.scheduleSlotList.band.bandName != "N/A"
        ) {
            Toasty.warning(context!!, "Slot already reserved", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedSlot.scheduleSlotList.band.state == "OK"
            && (selectedSlot.scheduleSlotList.band.uid != uid
                    || selectedSlot.scheduleSlotList.band.uid == uid)
        ) {
            Toasty.error(context!!, "Not Available", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedSlot.scheduleSlotList.band.state == "Pending"
            && selectedSlot.scheduleSlotList.band.uid == uid
        ) {
            Toasty.warning(context!!, "Book request already sent", Toast.LENGTH_SHORT).show()
            return false

        }
        return true


    }
    private fun viewSelectedSchedule(selectedSlot: ScheduleRow) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
            .inflate(R.layout.fragment_book_view_schedule, null)
        val bandPic: ImageView = inflater.findViewById(R.id.imageView_view_Schedule)
        val bandName: TextView = inflater.findViewById(R.id.bandName_viewSchedule)
        val endTime: TextView = inflater.findViewById(R.id.endTime_viewSchedule)
        val startTime: TextView = inflater.findViewById(R.id.startTime_viewSchedule)
        val spotifyIcon: ImageView = inflater.findViewById(R.id.social_media_spotify)
        val facebookIcon: ImageView = inflater.findViewById(R.id.social_media_facebook)
        val youtubeIcon: ImageView = inflater.findViewById(R.id.social_media_youtube)

        val uid = auth.currentUser?.uid
        if (selectedSlot.scheduleSlotList.band.isAnonymous &&
            selectedSlot.scheduleSlotList.band.uid != uid
        ) {
            bandPic.setImageResource(R.drawable.anonymous_bandpic)
            bandName.text = "Anonymous"

            //Anonymous social medias
            spotifyIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
            facebookIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
            youtubeIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
        } else {
            Picasso.get().load(selectedSlot.scheduleSlotList.band.profileImage)
                .centerCrop().fit().into(bandPic)
            bandName.text = selectedSlot.scheduleSlotList.band.bandName

            val selectedBandUid = selectedSlot.scheduleSlotList.band.uid
            //Launces Spotify
            spotifyIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Spotify")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val spotifyLink = p0.getValue(String::class.java) ?: return

                            SocialMediaLinkage.launchSpotifySocialMedia(spotifyLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })

            }
            //Launches facebook
            facebookIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Facebook")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val facebookLink = p0.getValue(String::class.java) ?: return

                            SocialMediaLinkage.launchFacebookSocialMedia(facebookLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })

            }
            //Launches youtube
            youtubeIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Youtube")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val youtubeLink = p0.getValue(String::class.java) ?: return
                            SocialMediaLinkage.launchYoutubeSocialMedia(youtubeLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
            }
        }
        endTime.text = selectedSlot.scheduleSlotList.endTime
        startTime.text = selectedSlot.scheduleSlotList.startTime

        builder.setView(inflater).create().show()

    }

    private fun viewSelectedSchedule2(selectedSlot: ScheduleRow){

        val inflater = LayoutInflater.from(context)
            .inflate(R.layout.fragment_book_view_schedule, null)
        val bandPic: ImageView = inflater.findViewById(R.id.imageView_view_Schedule)
        val bandName: TextView = inflater.findViewById(R.id.bandName_viewSchedule)
        val endTime: TextView = inflater.findViewById(R.id.endTime_viewSchedule)
        val startTime: TextView = inflater.findViewById(R.id.startTime_viewSchedule)
        val spotifyIcon: ImageView = inflater.findViewById(R.id.social_media_spotify)
        val facebookIcon: ImageView = inflater.findViewById(R.id.social_media_facebook)
        val youtubeIcon: ImageView = inflater.findViewById(R.id.social_media_youtube)

        val uid = auth.currentUser?.uid
        if (selectedSlot.scheduleSlotList.band.isAnonymous &&
            selectedSlot.scheduleSlotList.band.uid != uid
        ) {
            bandPic.setImageResource(R.drawable.anonymous_bandpic)
            bandName.text = "Anonymous"

            //Anonymous social medias
            spotifyIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
            facebookIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
            youtubeIcon.setOnClickListener {
                Toasty.error(context!!, "Identity hidden", Toast.LENGTH_SHORT).show()
            }
        } else {
            Picasso.get().load(selectedSlot.scheduleSlotList.band.profileImage)
                .centerCrop().fit().into(bandPic)
            bandName.text = selectedSlot.scheduleSlotList.band.bandName

            val selectedBandUid = selectedSlot.scheduleSlotList.band.uid
            //Launces Spotify
            spotifyIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Spotify")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val spotifyLink = p0.getValue(String::class.java) ?: return

                            SocialMediaLinkage.launchSpotifySocialMedia(spotifyLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })

            }
            //Launches facebook
            facebookIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Facebook")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val facebookLink = p0.getValue(String::class.java) ?: return

                            SocialMediaLinkage.launchFacebookSocialMedia(facebookLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })

            }
            //Launches youtube
            youtubeIcon.setOnClickListener {
                database.getReference("SocialMedia/$selectedBandUid")
                    .child("Youtube")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val youtubeLink = p0.getValue(String::class.java) ?: return
                            SocialMediaLinkage.launchYoutubeSocialMedia(youtubeLink)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
            }
        }
        endTime.text = selectedSlot.scheduleSlotList.endTime
        startTime.text = selectedSlot.scheduleSlotList.startTime

        val dialog =Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(inflater)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.window?.setLayout(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)



        dialog.show()



    }
    private fun fetchTodaySchedule() {

        val uid = auth.currentUser?.uid
        adapter.clear()
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayUnix = cal.time


        val ref = database.getReference("Schedules/$todayUnix/").child("slot")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val scheduleSlot = it.getValue(ScheduleSlot::class.java) ?: return
                    adapter.add(
                        ScheduleRow(
                            scheduleSlot,
                            todayUnix.toString(), uid!!
                        )

                    )

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }
    private fun fetchSelectedSchedule(selectedDate: String) {

        adapter.clear()
        val uid = auth.currentUser?.uid
        val ref = database.getReference("Schedules/$selectedDate/").child("slot")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {

                    val scheduleSlot = it.getValue(ScheduleSlot::class.java) ?: return
                    adapter.add(
                        ScheduleRow(
                            scheduleSlot,
                            selectedDate, uid!!
                        )
                    )
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }
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
    private fun querySchedByTime(startTime: Int, endTime: Int) {

        adapter.clear()
        val uid = auth.currentUser?.uid
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
        val ref = database.getReference("Schedules")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val schedule = it.getValue(Schedule::class.java) ?: return
                    val formattedTime = format.format(schedule.epouchValue)

                    val ref1 = database.getReference("Schedules/$formattedTime/slot")

                    ref1.addListenerForSingleValueEvent(object : ValueEventListener {


                        override fun onDataChange(p0: DataSnapshot) {
                            val dataSnapshots = p0.children.iterator()
                            val slots = ArrayList<ScheduleSlot>()
                            while (dataSnapshots.hasNext()) {
                                val dataSnapshotChild = dataSnapshots.next()
                                val slot = dataSnapshotChild.getValue(ScheduleSlot::class.java)
                                slots.add(slot!!)

                            }

                            for (i in (startTime - 8) until (endTime - 8)) {
                                adapter.add(ScheduleRow(slots[i], formattedTime, uid!!))

                            }
                        }


                        override fun onCancelled(p0: DatabaseError) {

                        }


                    })


                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

        queryType = "TIME"


    }
    private fun queryBySelectedDow(dayOfWeek: String) {

        adapter.clear()
        val uid = auth.currentUser?.uid
        val formatDoW = SimpleDateFormat("EEEE")
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
        val ref = database.getReference("Schedules")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val schedule = it.getValue(Schedule::class.java) ?: return
                    val formattedDoW = formatDoW.format(schedule.epouchValue)

                    if (formattedDoW == dayOfWeek) {
                        val formattedEpoch = format.format(schedule.epouchValue)
                        val ref1 = database.getReference("Schedules/$formattedEpoch/slot")
                        ref1.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {

                                p0.children.forEach {
                                    val slot = it.getValue(ScheduleSlot::class.java)
                                    adapter.add(ScheduleRow(slot!!, formattedEpoch, uid!!))

                                }


                            }

                            override fun onCancelled(p0: DatabaseError) {

                            }
                        })

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

        queryType = "DOW"
    }
    @TargetApi(23)
    private fun showTimePickerEnd(timeStart: Int) {

        //Select End Time
        val inflaterEnd = LayoutInflater.from(context!!).inflate(R.layout.dialog_time_picker, null)
        val timePicker: TimePicker = inflaterEnd.findViewById(R.id.dialog_time_picker)
        timePicker.setIs24HourView(true)
        timePicker.currentHour = 0
        timePicker.currentMinute = 0
        AlertDialog.Builder(context!!).setTitle("Select Time Slot(End)")
            .setPositiveButton("Search", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val timeSelectedEnd = timePicker.hour

                    if (timeSelectedEnd < 8 || timeSelectedEnd > 22) {
                        Toasty.error(
                            context!!, "Time Slot Invalid." +
                                    "\n Open Time: 8am " +
                                    "\n Closes: 10pm"
                        ).show()
                        return
                    }

                    querySchedByTime(timeStart, timeSelectedEnd)

                }

            })
            .setView(inflaterEnd).create().show()

    }
    @TargetApi(23)
    private fun showTimePickerStart() {


        val inflaterStart =
            LayoutInflater.from(context!!).inflate(R.layout.dialog_time_picker, null)
        val timePickerStart: TimePicker = inflaterStart.findViewById(R.id.dialog_time_picker)
        timePickerStart.setIs24HourView(true)
        timePickerStart.currentHour = 0
        timePickerStart.currentMinute = 0

        //Select Start Time
        AlertDialog.Builder(context!!).setTitle("Select Time Slot(Start)")
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val timeSelectedStart = timePickerStart.hour

                    if (timeSelectedStart < 8 || timeSelectedStart > 14) {
                        Toasty.error(
                            context!!, "Time Slot Invalid." +
                                    "\n Open Time: 8am " +
                                    "\n Closes: 10pm"
                        ).show()
                        return
                    }
                    dialog?.dismiss()
                    showTimePickerEnd(timeSelectedStart)
                }
            })

            .setView(inflaterStart).create().show()
    }
    private fun showSearchMenuDialog() {

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.search_menu_itemString_BookFragment)
            .setItems(
                R.array.SearchMenuItemsBookFragment
            ) { dialog, which ->
                when (which) {
                    0 -> showDatePicker()
                    1 -> showTimePickerStart()
                    2 -> showDayOfWeekDialog()
                    else -> dialog.dismiss()
                }

            }
        builder.create().show()


    }
    private fun showDayOfWeekDialog() {

        val inflater = LayoutInflater.from(context).inflate(R.layout.spinner_query_dayofweek, null)
        val dayOfWeekSpinnr: Spinner = inflater.findViewById(R.id.day_of_week_spinner)
        val adapterDayOfWeek = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.query_day_of_week_array)
        )
        adapterDayOfWeek.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        dayOfWeekSpinnr.adapter = adapterDayOfWeek
        val builder = AlertDialog.Builder(context)
            .setTitle("Select Day Of Week")
            .setPositiveButton("Search", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    queryBySelectedDow(dayOfWeekSpinnr.selectedItem.toString())
                }
            })


        builder.setView(inflater).create().show()


    }
    private fun showSnackBar() {

        Snackbar.make(
            activity?.findViewById(android.R.id.content)!!,
            "Book Request Sent",
            Snackbar.LENGTH_LONG
        ).setAction("Undo",object :View.OnClickListener{
            override fun onClick(v: View?) {
//             undoBook()
            }
        }).show()

    }

    //To be continued
    private fun undoBook(){
        Toast.makeText(context,""+ lastItemSelected,Toast.LENGTH_SHORT).show()
        val lastSelectedItem = adapter.getItem(lastItemSelected!!) as ScheduleRow
        val ref = database.getReference("Schedules/${lastSelectedItem.selectedDate}/slot/")


    }
    private fun showDatePicker() {

        var monthTitle: String?
        showDialog()
        fetchAvailableDates()


        datePicker.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                fetchSelectedSchedule(dateClicked.toString())


            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                monthTitle = monthFormat.format(firstDayOfNewMonth)
                Toast.makeText(
                    context, monthTitle
                    , Toast.LENGTH_SHORT
                ).show()

            }
        })


    }
    private fun showDialog() {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.dialog_date, null)

        datePicker = v.findViewById(R.id.dialog_date_picker)
        datePicker.setFirstDayOfWeek(Calendar.MONDAY)
        AlertDialog.Builder(context)
            .setView(v)
            .setTitle("Choose Date")
            .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            }
            )
            .create().show()

    }
    private fun showBookAsAnonymousDialog() {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Book as Anonymous?").setCancelable(false)
            .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    isAnonymous = true
                    img_anonymous.visibility = View.VISIBLE

                }
            })
            .setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    isAnonymous = false
                    img_anonymous.visibility = View.GONE
                }
            }).create().show()


    }
    private fun showCurrentDateTime() {

        val date = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MMM dd yyyy")
        val dateString = dateFormat.format(date)
        date_bookFragment.text = dateString

    }


}


