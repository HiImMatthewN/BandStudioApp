package com.example.bandstudioapp.IntroScreen;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.bandstudioapp.Main.DashboardActivity;
import com.example.bandstudioapp.Model.Client;
import com.example.bandstudioapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button btnNext;
    int position = 0;
    Button btnGetStarted;
    Animation btnAnim;
    TextView tvSkip;

    FirebaseAuth auth;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make the activity on full screen

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (!restorePrefData()) {
            Intent mainActivity = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(mainActivity);
            finish();

        }
        setContentView(R.layout.activity_intro);


        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
        tvSkip = findViewById(R.id.tv_skip);


        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Book rehearsals",
                "Directly book your band rehearsals without any hassle."
                , R.drawable.book_schedule));
        mList.add(new ScreenItem("Show your band's gigs",
                "Showcase your oncoming gigs. Bands support bands."
                , R.drawable.show_gigs));
        mList.add(new ScreenItem("Easy payment",
                "Paying has never been easy with services from G-cash to Cebuana Padala."
                , R.drawable.easy_payment));

        // setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        screenPager.setAdapter(introViewPagerAdapter);


        tabIndicator.setupWithViewPager(screenPager);


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = screenPager.getCurrentItem();
                if (position < mList.size()) {

                    position++;
                    screenPager.setCurrentItem(position);

                }

                if (position == mList.size() - 1) { // when we rech to the last screen

                    // TODO : show the GETSTARTED Button and hide the indicator and the next button

                    loadLastScreen();

                }

            }
        });

        // tablayout add change listener

        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == mList.size() - 1) {

                    loadLastScreen();

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Get Started button click listener

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //open main activity

                Intent mainActivity = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(mainActivity);
                // also we need to save a boolean value to storage so next time when the user run the app
                // we could know that he is already checked the intro screen activity
                // i'm going to use shared preferences to that process
                savePrefsData();
                finish();


            }
        });

        // skip button click listener

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenPager.setCurrentItem(mList.size());
            }
        });


    }


    private boolean restorePrefData() {

        final String currentUser = auth.getCurrentUser().getUid();
        SharedPreferences preferences = getSharedPreferences(currentUser, MODE_PRIVATE);
        preferences.edit().remove(currentUser).apply();
        DatabaseReference ref = database.getReference("/clients/" + currentUser);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Client client = dataSnapshot.getValue(Client.class);
                //stores to shared preference 'client.getFirstTime' boolean first time
                // true if first time, false if not.
                if (client != null) {
                    if (client.getFirstTime()) {
                        SharedPreferences.Editor editor = getSharedPreferences(currentUser, MODE_PRIVATE).edit();
                        editor.putBoolean("value", true);
                        editor.apply();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        SharedPreferences prefs = getSharedPreferences("Value", MODE_PRIVATE);
        boolean value = prefs.getBoolean(currentUser,true);
//        Toast.makeText(this,""+value,Toast.LENGTH_SHORT).show();
        return prefs.getBoolean(currentUser, false);


    }

    //Updates firstTime field to false
    private void savePrefsData() {
        String currentUser = auth.getUid();
        DatabaseReference ref = database.getReference("/clients/" + currentUser);
        if (currentUser != null)
            ref.child("firstTime").setValue(false);


    }

    // show the GETSTARTED Button and hide the indicator and the next button
    private void loadLastScreen() {

        btnNext.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tvSkip.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        // TODO : ADD an animation the getstarted button
        // setup animation
        btnGetStarted.setAnimation(btnAnim);

    }

}

