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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    private JSONObject jsonObject;
    private LinearLayout groceryTable;
    private int rowId = 0;
    private Map <Integer, String> groceries = new HashMap<Integer, String>();
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_in_photo);

        groceryTable = findViewById(R.id.scroll_view_add_layout);

        // json 파일 try-catch
        try {
            jsonObject = getPhotoResult();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 가져온 json 파일이 있다면 목록 출력
        if(jsonObject != null) {
            outputTable();
        }

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

                boolean nextPageLoad = true;
                String [] groceryList = new String[groceries.size()];
                for(Integer i : groceries.keySet()) {
                    if(groceries.get(i).contains("부위")){
                        nextPageLoad = false;
                        break;
                    }
                    else {
                        groceryList[i] = groceries.get(i);
                    }
                }

                if(nextPageLoad) {
                    intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                    intent.putExtra("groceryList", groceryList);
                    startActivity(intent);
                }
                else {
                    Log.e(TAG, "선택해주세요 알림 띄우자");
                }
            }
        });
    }

    // AWS에서 가져온 json 파일에서 필요한 데이터 빼오기
    private JSONObject getPhotoResult() throws IOException {
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
            return  new JSONObject(buffer.toString());
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 사진 속 식재료를 테이블로 출력
    public void outputTable() {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String grocery_name = keys.next().toString();
            int grocery_count = 0;
            try {
                grocery_count = jsonObject.getInt(grocery_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 목록 테이블에 삽입 - 고기의 경우 스피너(드롭다운) 출력
            if(grocery_name.contains("고기")) {
                String meet = new String();
                switch (grocery_name) {
                    case "닭고기": meet = "닭고기"; break;
                    case "소고기": meet = "소고기"; break;
                    default: meet = "돼지고기"; break;
                }

                for(int i = 0; i < grocery_count; i++)
                    makeTableWithSpinner(meet, grocery_name, Integer.toString(grocery_count));
            }
            else {
                makeTable(grocery_name, Integer.toString(grocery_count));
                groceries.put(rowId++, grocery_name);
            }
        }
    }

    public void textViewStyle(TextView textView) {
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }

    // 가져온 데이터 출력
    public void makeTable(String name, String cnt) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.no_meet_drop_down, null);

        TextView nameTextView = view.findViewById(R.id.no_meet_name);
        nameTextView.setText(name);
        textViewStyle(nameTextView);

        TextView noMeetTextView = view.findViewById(R.id.no_meet);
        textViewStyle(noMeetTextView);

        TextView countTextView = view.findViewById(R.id.no_meet_count);
        countTextView.setText(cnt);
        textViewStyle(countTextView);

        groceryTable.addView(view);
    }

    // spinner와 데이터 출력
    public void makeTableWithSpinner(String meetArray, String name, String count) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.meet_drop_down, null);

        TextView nameTextView = view.findViewById(R.id.meet_row_name);
        nameTextView.setText(name);
        textViewStyle(nameTextView);

        Spinner spinner = view.findViewById(R.id.meet_row_spinner);
        ArrayAdapter<CharSequence> adapter;
        switch (meetArray){
            case "닭고기":
                adapter = ArrayAdapter.createFromResource(this, R.array.chickenArray, android.R.layout.simple_dropdown_item_1line);
                break;
            case "소고기" :
                adapter = ArrayAdapter.createFromResource(this, R.array.beefArray, android.R.layout.simple_dropdown_item_1line);
                break;
            default:
                adapter = ArrayAdapter.createFromResource(this, R.array.porkArray, android.R.layout.simple_dropdown_item_1line);
                break;
        }

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        checkSpinner(rowId++, name, spinner);

        groceryTable.addView(view);
    }

    public void checkSpinner(int spinnerId, String name, Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                groceries.put(spinnerId, name+" "+item);
                if(item.contains("부위 선택"))
                    spinner.setBackgroundColor(Color.rgb(255, 110, 110));
                else
                    spinner.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
