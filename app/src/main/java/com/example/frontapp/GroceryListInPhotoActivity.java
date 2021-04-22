package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.Inflater;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    TableLayout groceryTable;
    Spinner spinner;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_in_photo);

//         json 파일 try-catch
//        try {
//            getPhotoResult();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // spinner 생성
        makeSpinner();

        // 목록 수정 버튼 클릭
        findViewById(R.id.grocery_list_in_photo_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "목록 수정 버튼");
            }
        });

        // NEXT 추천 레시피 목록 출력
        findViewById(R.id.grocery_list_in_photo_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "추천 레시피 목록");
            }
        });
    }

    // AWS에서 가져온 json 파일에서 필요한 데이터 빼오기
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

                // 목록 테이블에 삽입
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
        textViewStyle(groceryName);
        tableRow.addView(groceryName);

        etc.setText("아아아아");    // 이 자리에 선택하는거 넣어야함
        textViewStyle(etc); // d이거
        tableRow.addView(etc); // 이거

        groceryCount.setText(cnt);
        textViewStyle(groceryCount);
        tableRow.addView(groceryCount);

        groceryTable.addView(tableRow);
    }

    public void textViewStyle(TextView textView) {
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }

    // spinner 생성
    public void makeSpinner() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.meet_drop_down, null);
        ConstraintLayout linearLayout = view.findViewById(R.id.meet_row);
        spinner = linearLayout.findViewById(R.id.meet_spinner);
        Log.e(TAG,"집1");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.meetArray, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Log.e(TAG,"집12");
        spinner.setAdapter(adapter);
        Log.e(TAG,"집123");

        ConstraintLayout test = findViewById(R.id.linearLayout);
        test.addView(view);
        Log.e(TAG,"집1234");
    }
}
