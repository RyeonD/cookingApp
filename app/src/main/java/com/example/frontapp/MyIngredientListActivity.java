package com.example.frontapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyIngredientListActivity extends AppCompatActivity {
    private static String TAG = "MyIngredientListActivity";

    private static final String PREF_USER_ID = "MyAutoLogin";
    SharedPreferences sharedPreferencesUser;

    private static final String PREF_USER_INGREDIENT = "MyIngredientList";
    SharedPreferences sharedPreferencesUserIngredient;

    SharedPreferences.Editor editor;

    private GridLayout ingredientGrid;

    private int fresh_first = 0;
    private int fresh_second = 0;
    private int fresh_third = 0;
    private int fresh_fourth = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_management_test);

        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        getUserName();

        ingredientGrid = findViewById(R.id.grid_layout);
        sharedPreferencesUserIngredient = getSharedPreferences(PREF_USER_INGREDIENT, MODE_PRIVATE);
        getIngredientList();
    }

    // User의 Name 가져오기
    public void getUserName() {
        String name = sharedPreferencesUser.getString("UserName", "");

        TextView textView = findViewById(R.id.user_name_text);
        textView.setText(name);
    }

    // User의 재료 재고 목록 가져오기
    public void getIngredientList() {
        String array = sharedPreferencesUserIngredient.getString("ingredientList", "");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String ingredient = null;
            String freshness = null;
            // json 객체 생성 및 파싱
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject ingredientObject = (JSONObject) jsonArray.get(i);
                ingredient = ingredientObject.getString("sortkey");
                freshness = ingredientObject.getString("freshness");
                Log.e(TAG, ingredient+" / "+freshness);

                setIngredientList(ingredient, freshness);
                countFresh(freshness);
                setGroceryCount();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 재고 목록 출력
    private void setIngredientList(String ingredient, String freshness) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientView = inflater.inflate(R.layout.ingredient_object, null, false);
        TextView textView = ingredientView.findViewById(R.id.ingredient_object_name);

        textView.setText(ingredient);

        switch (freshness) {
            case "신선": {
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.ingredient_corner_level1));
                break;
            }
            case "양호": {
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.ingredient_corner_level2));
                break;
            }
            case "위험": {
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.ingredient_corner_level3));
                break;
            }
            default: {
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.ingredient_corner_level4));
                break;
            }
        }

        ingredientGrid.addView(ingredientView);
    }

    // 재고 리스트 계산(신선도 기준)
    private void countFresh(String freshness){
        switch (freshness) {
            case "신선": fresh_first++; break;
            case "양호": fresh_second++; break;
            case "위험": fresh_third++; break;
            default: fresh_fourth++; break;
        }
    }

    private void setGroceryCount() {
        TextView level1 = findViewById(R.id.level1_cnt);
        TextView level2 = findViewById(R.id.level2_cnt);
        TextView level3 = findViewById(R.id.level3_cnt);
        TextView level4 = findViewById(R.id.level4_cnt);

        level1.setText(Integer.toString(fresh_first));
        level2.setText(Integer.toString(fresh_second));
        level3.setText(Integer.toString(fresh_third));
        level4.setText(Integer.toString(fresh_fourth));
    }
}
