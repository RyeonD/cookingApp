package com.example.frontapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.PrecomputedText;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class GroceryManagementActivity extends AppCompatActivity {
    private static String TAG = "GroceryManagementActivity: ";
    private TableLayout groceryTable;
    private int gravity = Gravity.CENTER;
    private int paddingLeftRight = 0;
    private int paddingTopBottom = 40;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_management);

//        if(true) {
//            groceryTable = findViewById(R.id.grocery_manage_table);
//            if(true) // 고기라면
//                addTableRow("고기"+"("+"소고기"+")", "100", "21.05.05");
//
//            addTableRow("계란", "10", "21.05.10");
//            addTableRow("양파", "2", "21.05.07");
//        }

        findViewById(R.id.grocery_manage_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"재료 수정", Toast.LENGTH_LONG).show();
//                intent = new Intent(getApplicationContext(), GroceryManagementActivity.class);

            }
        });

        findViewById(R.id.grocery_manage_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] groceryList = {"소고기(안심)", "계란", "양파"};
                intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                intent.putExtra("groceryList", groceryList);
                startActivity(intent);
            }
        });
    }

    public void addTableRow(String name, String count, String date) {
        TableRow groceryTableRow = new TableRow(this);
        TextView nameTextView = new TextView(this);
        TextView countTextView = new TextView(this);
        TextView dateTextView = new TextView(this);

        groceryTableRow.setPadding(paddingLeftRight,paddingTopBottom,paddingLeftRight,paddingTopBottom);
        groceryTableRow.setGravity(gravity);
        textViewStyle(nameTextView);
        textViewStyle(countTextView);
        textViewStyle(dateTextView);

        // 여기에 값 입력
        nameTextView.setText(name);
        countTextView.setText(count);
        dateTextView.setText(date);

        groceryTableRow.addView(nameTextView);
        groceryTableRow.addView(countTextView);
        groceryTableRow.addView(dateTextView);
        groceryTable.addView(groceryTableRow);

        groceryTableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), name +" 눌림", Toast.LENGTH_LONG).show();

            }
        });
    }

    public void textViewStyle(TextView textView) {
        textView.setGravity(gravity);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }




}