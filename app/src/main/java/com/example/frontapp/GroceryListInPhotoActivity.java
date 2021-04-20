package com.example.frontapp;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    TableLayout groceryTable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_in_photo);

        try {
            getPhotoResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // json 파일에서 필요한 데이터 빼오기
    private void getPhotoResult() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/카메라인식결과.json";

        // 파일 가져오기
        try {
            InputStream data = assetManager.open(filename);
            InputStreamReader dataReader = new InputStreamReader(data);
            BufferedReader reader = new BufferedReader(dataReader);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }

            // json 객체 생성 및 파싱
            JSONObject jsonObject = new JSONObject(buffer.toString());
            Iterator<String> keys = jsonObject.keys();
            groceryTable = findViewById(R.id.grocery_list_in_photo_table);
            while (keys.hasNext()) {
                String grocery_name = keys.next().toString();
                int grocery_count = jsonObject.getInt(grocery_name);
                outputTable(grocery_name, Integer.toString(grocery_count));
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // 가져온 데이터 출력
    public void outputTable(String name, String cnt) {
        TableRow tableRow = new TableRow(this);
        TextView groceryName = new TextView(this);
        TextView groceryCount = new TextView(this);
        TextView etc = new TextView(this);

        tableRow.setPadding(0,40,0,40);
        tableRow.setGravity(Gravity.CENTER);
        groceryName.setText(name);
        etc.setText("아아아아");    // 이 자리에 선택하는거 넣어야함
        groceryCount.setText(cnt);

        textViewStyle(groceryName);
        textViewStyle(etc);
        textViewStyle(groceryCount);

        tableRow.addView(groceryName);
        tableRow.addView(etc);
        tableRow.addView(groceryCount);

        groceryTable.addView(tableRow);
    }

    public void textViewStyle(TextView textView) {
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }
}
