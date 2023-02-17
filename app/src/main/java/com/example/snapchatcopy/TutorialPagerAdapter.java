package com.example.snapchatcopy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialPagerAdapter extends PagerAdapter {
    LayoutInflater layoutInflater;
    List imgArray;
    List descArray;
    Context context;
    public TutorialPagerAdapter(Context context){
        this.context = context;
        imgArray = Arrays.asList(R.drawable.scan, R.drawable.click,
                R.drawable.list, R.drawable.searchbar);

        descArray = Arrays.asList("Center the ingredient label of a food product in the box and take the picture",
                "Click the \"Process Image\" button to process the ingredients or click back to retake the picture",
                "Only the harmful ingredients are displayed. Click on a harmful ingredient to learn more about what it is and why it's harmful",
                "Use the search feature to look up a food product's harmful ingredient information if you don't have it on hand");

    }


    @Override
    public int getCount() {
        return descArray.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.tutorial_slider, container, false);

        ImageView img = view.findViewById(R.id.scanImg);
        TextView desc = view.findViewById(R.id.description);

        img.setImageResource((Integer)imgArray.get(position));

        desc.setText((String)descArray.get(position));


        container.addView(view);
        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }



}
