package com.example.snapchatcopy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class IngredientInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_info);
        TextView info = findViewById(R.id.ingredientInfo);
        TextView _title = findViewById(R.id.ingredientTitle);
        TextView harmful = findViewById(R.id.ingredientHarmful);

        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title");
        String text = extras.getString("info");

        String[] ingredInfo = text.split("\n");

        _title.setText(title);
        info.setText(ingredInfo[0]);
        harmful.setText(ingredInfo[1]);
    }
}