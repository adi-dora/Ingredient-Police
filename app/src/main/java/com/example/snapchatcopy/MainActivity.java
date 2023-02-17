package com.example.snapchatcopy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    FragmentStateAdapter adapterViewPager;
    NavHostFragment navHostFragment;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        Log.d("NavController", navController.toString());

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);


        if(!sharedPreferences.contains("tutorialDone")){
            Log.d("New Device", "Added configs");
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("tutorialDone", false);

            editor.apply();
            Log.d("Shared Preferences", sharedPreferences.getAll().toString());
        }

        if(!sharedPreferences.getBoolean("tutorialDone", false)){
            Intent intent = new Intent(getApplicationContext(), TutorialActivity.class);
            startActivity(intent);
        }


        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(adapterViewPager);
        viewPager2.setCurrentItem(1);


    }

    public static class MyPagerAdapter extends FragmentStateAdapter {


        public MyPagerAdapter(FragmentManager supportFragmentManager, Lifecycle lifecycle) {
            super(supportFragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch(position){
                case 0:

                    return HistoryFragment.newInstance();
                case 1:
                    return CameraFragment.newInstance();

                case 2:
                    return SearchFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getItemCount() {

            return 3;
        }
    }
}