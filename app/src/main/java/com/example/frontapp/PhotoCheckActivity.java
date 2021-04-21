package com.example.frontapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_check);

//        Intent intent = getIntent();
//        byte[] bytes = intent.getByteArrayExtra("groceryImg");
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        // 다시 찍기
        Button captureBtn = findViewById(R.id.re_capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        // 요리 검색
        Button nextBtn = findViewById(R.id.search_next_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GroceryListInPhotoActivity.class);
                startActivity(intent);
            }
        });
    }
}
