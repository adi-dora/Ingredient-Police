package com.example.snapchatcopy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//FDC API key: DaDxCa6hMaBHnXcnIkkSp4GVY9verz7BMkT7xXKZ
public class SearchFragment extends Fragment {
    EditText mInput;
    ListView listView;
    SimpleAdapter arrayAdapter;
    BufferedReader reader;
    StringBuilder result;
    Thread searchThread;
    File f;
    JSONObject json;
    ArrayList<String> ingredients = new ArrayList<>();
    ArrayList<Integer> idList = new ArrayList<>();
    ImageButton camera_fragment, search_fragment, history_fragment;
    ViewPager2 pager;


    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    public static SearchFragment newInstance(){
        return new SearchFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mInput = view.findViewById(R.id.input);
        camera_fragment = view.findViewById(R.id.s_camera_fragment);
        search_fragment = view.findViewById(R.id.s_search_fragment);
        history_fragment = view.findViewById(R.id.s_history_fragment);
        pager = getActivity().findViewById(R.id.viewPager);
        Button mSearchButton = view.findViewById(R.id.search);
        ArrayList<String> prodHolder = new ArrayList<>();
        ArrayList<String> brandOwner = new ArrayList<>();

        listView = view.findViewById(R.id.productList);




        camera_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(1);
            }
        });

        search_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(2);
            }
        });

        history_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(0);
            }
        });
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String product = mInput.getText().toString();
                prodHolder.clear();
                ingredients.clear();

                if (product.length() == 0) {
                    Toast.makeText(requireContext(), "Please enter an item to search for",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                result = new StringBuilder();
                searchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("runs", "runs");
                        try {
                            URL url = new URL("https://api.nal.usda.gov/fdc/v1/foods/search?query=" +
                                    mInput.getText().toString()+
                                    "&dataType=Branded&pageSize=60&requireAllWords=true&sortOrder=asc" +
                                    "&api_key=DaDxCa6hMaBHnXcnIkkSp4GVY9verz7BMkT7xXKZ");
                            URLConnection conn = url.openConnection();
                            InputStream is = conn.getInputStream();
                            InputStreamReader inputReader = new InputStreamReader(is);
                            reader = new BufferedReader(inputReader);
                            for (String line; (line = reader.readLine()) != null; ) {

                                json = new JSONObject(line);
                                Log.d("Result", line);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try{
                            JSONArray foodData = (JSONArray)json.get("foods");
                            for(int i = 0; i < foodData.length(); i++){
                                JSONObject item = (JSONObject)(foodData.get(i));
                                prodHolder.add(item.getString("description"));
                                ingredients.add(item.getString("ingredients"));
                                brandOwner.add(item.getString("brandOwner"));
                                idList.add(item.getInt("fdcId"));

                            }

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                });

                searchThread.start();
                try {
                    searchThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String[] productArray = new String[prodHolder.size()];
                String[] brandArray = new String[prodHolder.size()];

                for(int i = 0; i < prodHolder.size(); i ++){
                    productArray[i] = prodHolder.get(i);
                }

                for(int i = 0; i < prodHolder.size(); i ++){
                    brandArray[i] = brandOwner.get(i);
                }

                //arrayAdapter = new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, prodHolder);
                SimpleListAdapter listAdapter = new SimpleListAdapter(productArray, brandArray);
                List<Map<String, String>> adaptedList = listAdapter.convertToListItems();
                arrayAdapter = new SimpleAdapter(getContext(), adaptedList, android.R.layout.simple_list_item_2,
                        new String[] {"text1", "text2"},new int[]{android.R.id.text1, android.R.id.text2});

                listView.setAdapter(arrayAdapter);

                listView.setOnItemClickListener((adapterView, view1, i, l) -> {

//                    String filename = "history";
//
//                    File file = new File(getContext().getCacheDir(), filename);
//                    if(!file.exists()){
//                        try {
//
//                            f = File.createTempFile(filename, ".TXT", requireContext().getCacheDir());
//
//                            Log.d("File Not Found", "New Cache Created");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    if(f.exists())
//                        Log.d("File exists", "Confirmed");
//
//                    List<String> newLines = new ArrayList<>();
//                    try {
//                        newLines.addAll(Files.readAllLines(Paths.get(f.getName()), StandardCharsets.UTF_8));
//                        newLines.add("" + idList.get(i) + "\n");
//                        Files.write(Paths.get(filename), newLines, StandardCharsets.UTF_8);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    Set<String> savedHistory = preferences.getStringSet("history", new HashSet<>());
                    Set<String> set = new HashSet<>();
                    if(savedHistory.size() != 0){

                        set.addAll(savedHistory);

                    }
                    SharedPreferences.Editor editor = preferences.edit();

                    if(!set.contains("" + idList.get(i)))

                        set.add("" + idList.get(i));
                    editor.putStringSet("history", set);
                    editor.apply();
                    Log.d("Edited Preferences", "New item added");


                    Intent intent = new Intent(getContext(), ProcessActivity.class);
                    Log.d("Product", prodHolder.get(i));
                    intent.putExtra("text", ingredients.get(i));
                    startActivity(intent);

                });
            }
        });




        return view;
    }

}
