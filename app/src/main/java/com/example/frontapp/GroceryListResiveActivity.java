package com.example.frontapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GroceryListResiveActivity extends AppCompatActivity {
    private static String TAG = "GroceryListResive";
    private Intent intent;
    private String [] groceryList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_resive);

        intent = getIntent();
        groceryList = intent.getStringArrayExtra("table");


    }
}
