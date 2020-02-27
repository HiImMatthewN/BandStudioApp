package com.example.bandstudioapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.GroupieAdapter.EventAdapter
import com.example.bandstudioapp.Model.Event
import com.example.bandstudioapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_event.*


class EventFragment : Fragment() {

    var adapter: EventAdapter? = null
    var events: List<Event>? = null
    private lateinit var datebase: FirebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        datebase = FirebaseDatabase.getInstance()

        showEvents()



    }


    private fun showEvents() {
        events = ArrayList()

        val ref = datebase.getReference("Events/")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val event = it.getValue(Event::class.java) ?: return

                    (events as ArrayList<Event>).add(
                        Event(
                            event.eventPosterUrl,
                            event.eventName,
                            event.eventVenue,
                            event.eventDate,
                            event.eventPrice,
                            event.productionName,
                            event.eventTime
                        )
                    )
                }




                (events as ArrayList<Event>).add(Event("https://i.redd.it/tle4mg3gujp11.jpg",
                    "Langit Lupa","The 70's bistro","Oct 6 2018",900,"Planet Indie","7:30 PM"))
                (events as ArrayList<Event>).add(Event("https://bandwagon-gig-finder.s3.amazonaws.com/editorials/uploads/pictures/15470/content_panic-at-the-disco-manila-poster-mmi-live-bandwagon.jpg",
                    "Panic at the Disco Live!","Mall Of Asia Arena","Oct 20 2018",3500,"MMI Live!","8:00 PM"))
                (events as ArrayList<Event>).add(Event("http://www.philippineconcerts.com/wp-content/uploads/2018/12/the-1975-live-in-manila-2019-1-460x460.jpg",
                    "The 1975 Live!","Mall Of Asia Arena","Sept 11 2019",5000,"Pulp Live!","8:00 PM"))
                initEventAdapter()
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })


    }

    private fun initEventAdapter() {
        adapter = EventAdapter(events as ArrayList<Event>, context)
        viewPager.adapter = adapter
        viewPager.setPadding(130, 0, 130, 0)
    }
}
