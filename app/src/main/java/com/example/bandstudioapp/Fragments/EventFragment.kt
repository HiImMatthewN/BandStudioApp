package com.example.bandstudioapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bandstudioapp.GroupieAdapter.EventAdapter
import com.example.bandstudioapp.Model.Event
import com.example.bandstudioapp.R
import kotlinx.android.synthetic.main.fragment_event.*


class EventFragment : Fragment() {

    var adapter: EventAdapter? = null

    var events: List<Event>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        events = ArrayList()
        (events as ArrayList<Event>).add(Event("https://i.redd.it/tle4mg3gujp11.jpg",
            "Langit Lupa","The 70's bistro","Oct 6 2018"))
        (events as ArrayList<Event>).add(Event("https://bandwagon-gig-finder.s3.amazonaws.com/editorials/uploads/pictures/15470/content_panic-at-the-disco-manila-poster-mmi-live-bandwagon.jpg",
            "Panic at the Disco Live!","Mall Of Asia Arena","Oct 20 2018"))
        (events as ArrayList<Event>).add(Event("http://www.philippineconcerts.com/wp-content/uploads/2018/12/the-1975-live-in-manila-2019-1-460x460.jpg",
            "The 1975 Live!","Mall Of Asia Arena","Sept 11 2019"))
        (events as ArrayList<Event>).add(Event("https://mmilive.com/wp-content/uploads/2019/10/GREENDAY_MNL-ADMAT-lowres.jpg",
            "Green Day Live!","Mall Of Asia Arena","March 3 2020"))
        adapter = EventAdapter(events as ArrayList<Event>,context)
        viewPager.adapter = adapter
        viewPager.setPadding(130,0,130,0)


    }
}
