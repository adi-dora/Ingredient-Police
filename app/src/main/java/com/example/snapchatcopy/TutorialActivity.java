package com.example.snapchatcopy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TutorialActivity extends AppCompatActivity {
    RelativeLayout tutorialActivity;
    AnimationDrawable drawable;
    ViewPager pager;
    TutorialPagerAdapter tutorialPagerAdapter;
    DotsIndicator dotsIndicator;
    Button closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        tutorialActivity = findViewById(R.id.tutorial);
        dotsIndicator = findViewById(R.id.dot_indicator);
        closeBtn = findViewById(R.id.close);

        drawable = (AnimationDrawable) tutorialActivity.getBackground();
        drawable.setEnterFadeDuration(2500);
        drawable.setExitFadeDuration(1500);
        drawable.start();

        pager = findViewById(R.id.tutorialPager);
        tutorialPagerAdapter = new TutorialPagerAdapter(this);
        pager.setAdapter(tutorialPagerAdapter);
        dotsIndicator.setViewPager(pager);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("tutorialDone", true);
                editor.apply();
                Log.d("Tutorial Finished", "tutorialDone state changed");
                Log.d("Shared Preferences", sharedPref.getAll().toString());


                finish();
            }
        });




    }
}