package com.example.snapchatcopy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ProcessActivity extends AppCompatActivity {
    Bundle extras;
    String text;
    TextView titleHolder, noHarmful;
    ListView ingredients;
    ArrayList<String[]> harmfulIngredients;
    ArrayList<String> usedKeys;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        titleHolder = findViewById(R.id.ingredientTitleHolder);
        noHarmful = findViewById(R.id.noHarmful);
        ingredients = findViewById(R.id.ingredientList);

        extras = getIntent().getExtras();
        text = (String) extras.get("text");


        harmfulIngredients = new ArrayList<>();
        usedKeys = new ArrayList<>();

        findHarmful(text);

        if(harmfulIngredients.size() == 0)
            noHarmful.setText("No Harmful Ingredients Found.");
        else{
            //ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, harmfulIngredients);
            ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_2,
            android.R.id.text1, harmfulIngredients){

                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View row = super.getView(position, convertView, parent);

                    TextView text1 = (TextView) row.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) row.findViewById(android.R.id.text2);
                    text2.setLines(1);

                    String[] item = harmfulIngredients.get(position);
                    text1.setText(item[0]);

                    text2.setText(item[1]);

                    return row;
                }
            };


            ingredients.setAdapter(arrayAdapter);

            ingredients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String[] item = harmfulIngredients.get(i);

                    Intent intent = new Intent(getApplicationContext(), IngredientInfoActivity.class);
                    intent.putExtra("title", item[0]);
                    intent.putExtra("info", item[1]);
                    startActivity(intent);

                }
            });
        }
    }


    public void findHarmful(String text)  {
        String[] splitText = text.split(",");
        String[] splitText2 = text.split(";");
        String[] mainArr;
        if(splitText2.length > splitText.length)
            mainArr = splitText2;
        else{
            mainArr = splitText;
        }
        ArrayList<String> allIngredients = new ArrayList<>(Arrays.asList(mainArr));
        Log.d("Ingredients", allIngredients.toString());
        try{
            InputStream is = getApplicationContext().getAssets().open("ingredients.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonObject = new JSONObject(new String(buffer, StandardCharsets.UTF_8));

        } catch(Exception e){
            e.printStackTrace();
        }

        Iterator<String> keys = jsonObject.keys();
        ArrayList<String> keysArray = Lists.newArrayList(keys);
        for(String ingredient : allIngredients){
            for(String key : keysArray) {
                String ingredientFiltered = ingredient.replaceAll("#", "").replaceAll(" ", "")
                        .replaceAll("\n", "").replaceAll("-", "").toUpperCase();

                String keyFiltered = key.replaceAll("#", "").replaceAll(" ", "")
                        .replaceAll("\n", "").replaceAll("-", "").toUpperCase();
                try {
                    if (ingredientFiltered.contains(keyFiltered) || keyFiltered.contains(ingredientFiltered)) {

                        if(!usedKeys.contains(key)){
                            harmfulIngredients.add(new String[]{key, jsonObject.getString(key)});
                            usedKeys.add(key);
                        }

                    }
                } catch (Exception ignored) { }
            }
        }
    }






}