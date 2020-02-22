package com.example.bandstudioapp.GroupieAdapter;

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
    private LayoutInflater layoutInflater;
    private Context context;

    public EventAdapter(ArrayList<Event> models, Context context) {
        this.models = models;
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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.fragment_event_item, container, false);

        ImageView imageView;
        TextView title, desc,date;


        imageView = view.findViewById(R.id.image_event_item);
        title = view.findViewById(R.id.title_event_item);
        desc = view.findViewById(R.id.desc_event_item);
        date = view.findViewById(R.id.date_event_item);

        Picasso.get().load(models.get(position).getEventImageUrl()).into(imageView);
        title.setText(models.get(position).getEventName());
        desc.setText(models.get(position).getEventVenue());
        date.setText(models.get(position).getEventDate());

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
