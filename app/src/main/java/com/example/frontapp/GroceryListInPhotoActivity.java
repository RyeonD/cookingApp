package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    // 재료 List 확인
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
                Toast.makeText(getApplicationContext(), "목록 수정 버튼 눌림", Toast.LENGTH_LONG).show();
            }
        });

        // NEXT 버튼 클릭(주재료 선택 페이지로 이동)
        findViewById(R.id.grocery_list_in_photo_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 부위가 선택되었는지 확인(선택되었으면 다음페이지로 이동. 선택되지 않았으면 알림 띄우고, 선택 유도)
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

                // 부위 선택됨 - 다음 페이지로 이동
                if(nextPageLoad) {
                    intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                    intent.putExtra("groceryList", groceryList);
                    startActivity(intent);
                }
                // 부위 선택되지 않음 - 선택 유도 알림창 띄움
                else {
                    showDialog();
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

    // 가져온 데이터(재료 목록) 출력
    public void makeTable(String name, String cnt) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.no_meet_drop_down, null);

        TextView nameTextView = view.findViewById(R.id.no_meet_name);
        nameTextView.setText(name);
        textViewStyle(nameTextView);

        TextView noMeetTextView = view.findViewById(R.id.no_meet);
        textViewStyle(noMeetTextView);

        groceryTable.addView(view);
    }

    // spinner(드롭다운) 설정
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

    // 드롭 다운 선택시 배경 변경 - "부위 선택"(선택하지 않음)=red, "부위 선택" 외(부위 선택됨)=white
    public void checkSpinner(int spinnerId, String name, Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                groceries.put(spinnerId, name+" "+item);
                if(item.contains("부위 선택"))
                    spinner.setBackgroundColor(Color.rgb(255, 110, 110));
                else
                    spinner.setBackgroundColor(Color.argb(0, 255, 255, 255));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // 알림창
    // 알림창
    public void showDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("알림")
                .setMessage("고기 부위가 선택되지 않았습니다. 부위를 선택해주세요.")
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "다시 선택");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
