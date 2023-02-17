package com.example.snapchatcopy;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoryFragment extends Fragment {

    ListView hist;
    TextView noSearches;
    ImageButton camera_fragment, history_fragment, search_fragment;
    ArrayList<String> product = new ArrayList<>();
    ArrayList<String> brandOwner = new ArrayList<>();
    ArrayList<String> ingredients = new ArrayList<>();
    ArrayList<String> idList = new ArrayList<>();
    JSONObject json;
    RelativeLayout loadingPanel;

    public static HistoryFragment newInstance(){
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        camera_fragment = view.findViewById(R.id.h_camera_fragment);
        search_fragment = view.findViewById(R.id.h_search_fragment);
        history_fragment = view.findViewById(R.id.h_history_fragment);
        noSearches = view.findViewById(R.id.noSearches);
        hist = view.findViewById(R.id.history);
        loadingPanel = view.findViewById(R.id.loadingPanel);





        camera_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraFragment.newInstance();
            }
        });

        search_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchFragment.newInstance();
            }
        });

        history_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryFragment.newInstance();
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Set<String> savedHistory = preferences.getStringSet("history", new HashSet<>());
        if(savedHistory.size() == 0){
            noSearches.setText("No Past Searches");
            loadingPanel.setVisibility(View.GONE);
            return;
        }

        for(String search : savedHistory) {
            String finalLine = search;
            try {

                Thread thread = new Thread(() -> {
                    try {
                        URL url = new URL("https://api.nal.usda.gov/fdc/v1/food/" + finalLine +
                                "?api_key=DaDxCa6hMaBHnXcnIkkSp4GVY9verz7BMkT7xXKZ");
                        URLConnection conn = url.openConnection();
                        InputStream is = conn.getInputStream();
                        InputStreamReader inputReader = new InputStreamReader(is);
                        BufferedReader reader1 = new BufferedReader(inputReader);
                        for (String line1; (line1 = reader1.readLine()) != null; ) {

                            json = new JSONObject(line1);
                            product.add(json.getString("description"));
                            brandOwner.add(json.getString("brandOwner"));
                            ingredients.add(json.getString("ingredients"));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }

        }

            String[] productArray = new String[product.size()];
            String[] brandArray = new String[product.size()];

            for(int i = 0; i < product.size(); i ++){
                productArray[i] = product.get(i);
            }

            for(int i = 0; i < product.size(); i ++){
                brandArray[i] = brandOwner.get(i);
            }

            SimpleListAdapter listAdapter = new SimpleListAdapter(productArray, brandArray);
            List<Map<String, String>> adaptedList = listAdapter.convertToListItems();
            SimpleAdapter arrayAdapter = new SimpleAdapter(getContext(), adaptedList, android.R.layout.simple_list_item_2,
                    new String[] {"text1", "text2"},new int[]{android.R.id.text1, android.R.id.text2});

            hist.setAdapter(arrayAdapter);
            loadingPanel.setVisibility(View.GONE);


            hist.setOnItemClickListener((adapterView, view1, i, l) -> {
                Intent intent = new Intent(getContext(), ProcessActivity.class);
                intent.putExtra("text", ingredients.get(i));
                startActivity(intent);

            });
        }

    @Override
    public void onPause() {
        super.onPause();
        loadingPanel.setVisibility(View.VISIBLE);
        hist.setAdapter(null);
        noSearches.setText("");
        idList.clear();
        product.clear();
        brandOwner.clear();

    }
}
