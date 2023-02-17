package com.example.snapchatcopy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;


public class PictureActivity extends AppCompatActivity {
    ImageButton mBack;
    Button mProcess;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        mBack = findViewById(R.id.back);
        mProcess = findViewById(R.id.process);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        byte[] b= extras.getByteArray("picture");

        ImageView image = findViewById(R.id.pictureCapture);



        if(b.length == 0) {
            Toast.makeText(getApplicationContext(), "An error occurred reading the image",
                    Toast.LENGTH_LONG).show();
            finish();

        }
        Log.d("Bytes Not Null", "Line 40");
        Bitmap decodeBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        imageBitmap = rotate(decodeBitmap);

        image.setImageBitmap(imageBitmap);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        mProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProcess.setEnabled(false);
                TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                InputImage image_ = InputImage.fromBitmap(imageBitmap, 0);
                Task<Text> task = textRecognizer.process(image_)


                        .addOnSuccessListener(text -> {
                            StringBuilder str = new StringBuilder();
                            if (text.getTextBlocks().size() == 0) {
                                Toast.makeText(getApplicationContext(), "No Text Found. Please scan again.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }

                            for (Text.TextBlock block : text.getTextBlocks()) {
                                String blockText = block.getText();
                                Log.d("Block Text:", blockText);
                                str.append(block.getText());
                            }

                            Intent intent = new Intent(getApplicationContext(), ProcessActivity.class);
                            intent.putExtra("text", str.toString());
                            startActivity(intent);


                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                e.printStackTrace();

                            }
                        });

                mProcess.setEnabled(true);


            }
        });



    }

    private Bitmap rotate(Bitmap bitmap){
        int width = bitmap.getWidth();

        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();

        matrix.setRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}