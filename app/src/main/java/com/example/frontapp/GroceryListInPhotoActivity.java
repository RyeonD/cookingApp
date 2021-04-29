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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    private JSONObject jsonObject;
    private LinearLayout groceryTable;
    private int rowId = 0;
    private Map <Integer, String> groceries = new HashMap<Integer, String>();
    private Intent intent;
    ArrayList<GroceryList> arrayList;
    GroceryListAdapter adapter;
    private String [] groceryList;

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
                groceryList = adapter.getNext();    // 부위 선택 완료: 재료 목록(String Array), 부위 선택 미완료: null

                // 부위 선택이 완료되었는지 확인
                if(groceryList != null) {
                    intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                    intent.putExtra("groceryList", groceryList);
                    startActivity(intent);
                }
                else {
                    showDialog();
                }
            }
        });
    }

    // AWS에서 가져온 json 파일에서 필요한 데이터 빼오기
    private JSONObject getPhotoResult() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/cameraResult.json";

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
        arrayList = new ArrayList<>();

        Iterator iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String s = iterator.next().toString();
            arrayList.add(new GroceryList(s));
        }

        // Adapter 생성
        adapter = new GroceryListAdapter(arrayList);

        ListView listView = findViewById(R.id.grocery_table_layout);
        listView.setAdapter(adapter);
    }

    // 알림창 - 부위 선택 미완료 시 띄워줌
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
