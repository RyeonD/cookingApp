package com.example.frontapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class CookInfoNGredient extends AppCompatActivity {
    private static String TAG = "CookInfoNGredient";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_info_gredient);

        Log.e(TAG, "성공");
        Intent intent = getIntent();
        Log.e(TAG, intent.getStringExtra("name"));

    }
}
