package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class IngredientManagementActivity extends AppCompatActivity {
    private static String TAG = "GroceryManagementActivity";
    private TableLayout groceryTable;
    private int gravity = Gravity.CENTER;
    private int paddingLeftRight = 0;
    private int paddingTopBottom = 40;
    Intent intent;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private Ingredient ingredient;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private IngredientAdapter adapter;
    private ArrayList <Ingredient> ingredientArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_management);

        // 데이터 가져와 출력
        getJsonArray();

        // 재고 목록 출력
        originalPage();

    }

    // JsonArray 가져오기
    public void getJsonArray() {
        AssetManager assetManager = getAssets();
        String filename = "jsons/ingredientListResult.json";
        InputStream data = null;

        try {
            data = assetManager.open(filename);
            InputStreamReader dataReader = new InputStreamReader(data);
            BufferedReader reader = new BufferedReader(dataReader);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }

            // json 객체 생성 및 파싱
            jsonObject = new JSONObject(buffer.toString());
            Iterator iterator = jsonObject.keys();

            while(iterator.hasNext()){
                jsonArray = jsonObject.getJSONArray(iterator.next().toString());
            }

            // 리스트 출력
            String name = null;
            String freshLevel = null;
            for(int i = 0; i < jsonArray.length(); i++) {
                jsonObject = (JSONObject) jsonArray.get(i);
                name = jsonObject.keys().next();
                freshLevel = jsonObject.getString(name);
                ingredientArrayList.add(new Ingredient(name, freshLevel));
            }

        } catch (JSONException|IOException e) {
            e.printStackTrace();
        }
    }

    // 재고 목록 페이지의 경우
    public void originalPage() {
        setContentView(R.layout.activity_ingredient_management);

        recyclerView = findViewById(R.id.ingredient_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new IngredientAdapter(ingredientArrayList, false);
        recyclerView.setAdapter(adapter);

        // 재고 목록 수정 버튼
        findViewById(R.id.ingredient_resive_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "재료 수정", Toast.LENGTH_LONG).show();
                listResivePage();
            }
        });

        // 재고 목록 안 재료들로 검색 버튼
        findViewById(R.id.cook_search_in_ingredient_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 넘겨줄 재료 목록 생성(재료 이름만 들어감)
                String[] groceryList = new String[ingredientArrayList.size()];
                for(int i = 0; i < groceryList.length; i++)
                    groceryList[i] = ingredientArrayList.get(i).getName();

                intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                intent.putExtra("groceryList", groceryList);
                startActivity(intent);
            }
        });
    }

    // 재고 목록 수정 Button
    public void listResivePage() {
        setContentView(R.layout.activity_ingredient_management_resive);

        recyclerView = findViewById(R.id.ingredient_recyclerview);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new IngredientAdapter(ingredientArrayList, true);
        recyclerView.setAdapter(adapter);

        // 재료 삭제 버튼
        findViewById(R.id.ingredient_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = ingredientArrayList.size()-1; i >= 0; i--) {
                    if(ingredientArrayList.get(i).isCheck())
                        ingredientArrayList.remove(i);
                }

                adapter = new IngredientAdapter(ingredientArrayList, true);
                recyclerView.setAdapter(adapter);
            }
        });

        // 재료 추가 버튼
        findViewById(R.id.ingredient_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "재료 추가할꺼다", Toast.LENGTH_LONG).show();
                groceryAddDialog();
            }
        });

        // 재료 수정 완료 버튼
        findViewById(R.id.ingredient_resive_complete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originalPage();
            }
        });
    }

    // 재고 목록 추가시 입력창 띄우기
    public void groceryAddDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(IngredientManagementActivity.this)
                .setTitle("알림")
                .setView(view)
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText textPaint = view.findViewById(R.id.edit_grocery_name);
                        ingredientArrayList.add(new Ingredient(textPaint.getText().toString(), "-"));
                        adapter = new IngredientAdapter(ingredientArrayList, true);
                        recyclerView.setAdapter(adapter);
                        Log.e(TAG,"확인");
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "취소할꺼임");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}