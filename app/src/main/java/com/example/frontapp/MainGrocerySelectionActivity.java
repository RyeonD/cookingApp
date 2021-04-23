package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainGrocerySelectionActivity extends AppCompatActivity {
    private static String TAG = "MainGrocerySelectionActivity: ";
    private Intent intent;
    private String [] groceryList;
    private LinearLayout scrollLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_in_photo);

        intent = getIntent();
        groceryList = intent.getStringArrayExtra("groceryList");
        scrollLayout = findViewById(R.id.scroll_view_add_layout);

        for(String s: groceryList)
            groceryListOutput(s);
    }

    // 리스트 출력
    public void groceryListOutput(String s) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main_grocery_selection, null);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setText(s);
        scrollLayout.addView(view);
    }
}
