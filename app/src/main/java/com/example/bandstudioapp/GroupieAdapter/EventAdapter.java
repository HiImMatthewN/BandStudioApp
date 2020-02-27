package com.example.bandstudioapp.GroupieAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.bandstudioapp.Model.Event;
import com.example.bandstudioapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EventAdapter extends PagerAdapter {
    private ArrayList<Event> models;
    private Context context;

    public EventAdapter(ArrayList<Event> models, Context context) {

        this.models = models;
        notifyDataSetChanged();
        this.context = context;


    }

    @Override
    public int getCount() {
            return models.size();


    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.fragment_event_item, container, false);

        ImageView posterIV;
        TextView titleTV, dateTV
                ,venueTV,priceTV
                ,productionNameTV,timeTV;


        posterIV = view.findViewById(R.id.event_poster);
        titleTV = view.findViewById(R.id.event_name);
        dateTV = view.findViewById(R.id.event_date);
        venueTV = view.findViewById(R.id.event_venue);
        priceTV = view.findViewById(R.id.event_price);
        productionNameTV = view.findViewById(R.id.event_production);
        timeTV  = view.findViewById(R.id.event_time);

        Picasso.get().load(models.get(position).getEventPosterUrl()).into(posterIV);
        titleTV.setText(models.get(position).getEventName());
        dateTV.setText(models.get(position).getEventVenue());
        venueTV.setText(models.get(position).getEventDate());
        priceTV.setText("₱" +models.get(position).getEventPrice());
        productionNameTV.setText(models.get(position).getProductionName());
        timeTV.setText(models.get(position).getEventTime());
        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
