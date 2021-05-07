package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyIngredientListActivity extends AppCompatActivity {
    private static String TAG = "MyIngredientListActivity";

    private static final String PREF_USER_ID = "MyAutoLogin";
    SharedPreferences sharedPreferencesUser;

//    private static final String PREF_USER_INGREDIENT = "MyIngredientList";
//    SharedPreferences sharedPreferencesUserIngredient;

    private GridLayout ingredientGrid;

    private int fresh_first = 0;
    private int fresh_second = 0;
    private int fresh_third = 0;
    private int fresh_fourth = 0;

    private JSONArray jsonArray;
    private ArrayList<MyIngredient> ingredientList;
    private ArrayList<String> deleteIngredient;
    private ArrayList<String> addIngredient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteIngredient = new ArrayList<>();
        addIngredient = new ArrayList<>();

        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        setOriginalPage();
    }

    // Orinal Page 출력
    private void setOriginalPage() {
        setContentView(R.layout.activity_my_ingredient_list);

        // 이름 출력
        TextView textView = findViewById(R.id.user_name_text);
        textView.setText(getUserName());

        getChangeIngredientList();

//        if(ingredientList != null){
//            getChangeIngredientList();
//        }
//        else
//            getIngredientList();

        findViewById(R.id.ingredient_page_edit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChangePage();
            }
        });
    }

    // 수정 Page 출력
    private void setChangePage() {
        setContentView(R.layout.activity_my_ingredient_list_resive);

        outputChangePage();

        deleteIngredient = new ArrayList<>();
        addIngredient = new ArrayList<>();

        // 재료 추가
        findViewById(R.id.ingredient_page_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientAddDialog();
            }
        });

        // 재료 삭제
        findViewById(R.id.ingredient_page_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "삭제 할 목록");
                for(int i = ingredientList.size()-1; i >= 0; i--) {
                    if(ingredientList.get(i).getCheckbox()) {
                        deleteIngredient.add(ingredientList.get(i).getName());
                        ingredientList.remove(i);
                    }
                }
                outputChangePage();
            }
        });

        // 수정 완료
        findViewById(R.id.ingredient_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOriginalPage();
                Log.e(TAG, addIngredient+"?");
                Log.e(TAG, deleteIngredient+"?");
            }
        });
    }

    // User의 재료 재고 목록 가져오기 - 서버에서 가져오는 것 구현 필요
    private void getIngredientList1() {
        ingredientList = new ArrayList<>();     // 수정 후 기존 재료 목록에 수정된 재료 목록이 추가되는 것을 방지

        // json 파일 try-catch
        AssetManager assetManager = getAssets();
        String filename = "jsons/loginResult.json";

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

            jsonArray = new JSONArray(buffer.toString());
            Log.e(TAG, jsonArray.toString());

            JSONObject jsonObject = null;
            String name = null;
            String freshness = null;
            for(int i=0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                name = jsonObject.get("sortkey").toString();
                freshness = jsonObject.get("freshness").toString();
                ingredientList.add(new MyIngredient(name, freshness));
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // original 페이지의 목록 출력
    private void outputOriginalPage() {
        ingredientGrid = findViewById(R.id.grid_layout);
        ingredientGrid.removeAllViews();

        for(MyIngredient ingredient:ingredientList){
            setIngredientListInOrigin(ingredient);
            countFresh(ingredient.getFreshness());
        }

        setFreshCount();
    }

    private void outputChangePage() {
        ingredientGrid = findViewById(R.id.grid_layout);
        ingredientGrid.removeAllViews();

        for(MyIngredient ingredient:ingredientList){
            setIngredientListInChange(ingredient);
        }
    }

    // 재고 목록 출력 - Origin Page
    private void setIngredientListInOrigin(MyIngredient ingredient) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientView = inflater.inflate(R.layout.ingredient_object, null, false);
        TextView textView = ingredientView.findViewById(R.id.ingredient_object_name);

        textView.setText(ingredient.getName());
        textView.setBackground(ingredient.getBackground(this));

        ingredientGrid.addView(ingredientView);
    }

    // 재고 목록 출력 - 수정 페이지
    private void setIngredientListInChange(MyIngredient ingredient) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientView = inflater.inflate(R.layout.ingredient_object_check, null, false);
        ConstraintLayout constraintLayout = ingredientView.findViewById(R.id.ingredient_box);
        TextView textView = ingredientView.findViewById(R.id.ingredient_object_name);
        CheckBox checkBox = ingredientView.findViewById(R.id.ingredient_check);

        textView.setText(ingredient.getName());
        constraintLayout.setBackground(ingredient.getBackground(this));

        ingredientGrid.addView(ingredientView);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    ingredient.setCheckbox(true);
                }
                else {
                    ingredient.setCheckbox(false);
                }
                Log.e(TAG, String.valueOf(ingredient.getCheckbox()));
            }
        });
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

    // fresh 개수 출력
    private void setFreshCount() {
        TextView level1 = findViewById(R.id.level1_cnt);
        TextView level2 = findViewById(R.id.level2_cnt);
        TextView level3 = findViewById(R.id.level3_cnt);
        TextView level4 = findViewById(R.id.level4_cnt);

        level1.setText(Integer.toString(fresh_first));
        level2.setText(Integer.toString(fresh_second));
        level3.setText(Integer.toString(fresh_third));
        level4.setText(Integer.toString(fresh_fourth));
    }

    // 입력 방법 선택 dialog 창
    private void ingredientAddDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyIngredientListActivity.this)
                .setTitle("입력 방법 선택")
                .setMessage("텍스트 입력 -> 직접 입력\n재료를 촬영하여 자동 입력 -> 사진 촬영")
                .setPositiveButton("직접 입력", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDialog();
                    }
                }).setNegativeButton("사진 촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "사진 촬영");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    // 재료 추가 - 직접 입력 창
    private void addDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyIngredientListActivity.this)
                .setTitle("재료 추가")
                .setView(view)
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText textPaint = view.findViewById(R.id.edit_grocery_name);
                        checkIngredient(textPaint.getText().toString());
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

    // UserID 가져오기
    public String getUserId() {
        return sharedPreferencesUser.getString("UserId", "");
    }

    // User의 Name 가져오기
    public String getUserName() {
        return sharedPreferencesUser.getString("UserName", "");
    }

    // DB에서 재료 목록 가져오기
    private void getIngredientList() {
        ingredientList = new ArrayList<>();
        // 데이터 가져오기
        RetrofitClass retrofitClass = new RetrofitClass(5000);
        MainInterface api = retrofitClass.retrofit.create(MainInterface.class);
        Log.e(TAG, "확인"+getUserId());
        Call<String> call = api.getUserId(getUserId());
        call.enqueue(new Callback<String>()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e("onSuccess", response.body());

                    String jsonResponse = response.body();
                    try {
                        JSONObject jsonObject = new JSONObject( jsonResponse );
                        Log.e(TAG, jsonObject.toString());
                        if (jsonObject.getString("success").equals("true")) {
                            jsonArray = jsonObject.getJSONArray("result");

                            // 받아온 데이터(나의 재료 재고 목록) 전체 출력
                            Log.e(TAG, jsonArray.toString());

                            // 받아온 데이터 출력
                            String name = null;
                            String freshness = null;
                            for(int i=0; i < jsonArray.length(); i++) {
                                jsonObject = jsonArray.getJSONObject(i);
                                name = jsonObject.get("sortkey").toString();
                                freshness = jsonObject.get("freshness").toString();
                                ingredientList.add(new MyIngredient(name, freshness));
                            }
                            outputOriginalPage();
                        } else {
                            Toast.makeText( getApplicationContext(), "레시피 가져오기에 실패했습니다.", Toast.LENGTH_SHORT ).show();
                            return;
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "로그 없음");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // DB에서 재료 목록 가져오기 & 재료 수정
    private void getChangeIngredientList() {
        ingredientList = new ArrayList<>();
        // 데이터 가져오기
        RetrofitClass retrofitClass = new RetrofitClass(5000);
        MyIngredientList api = retrofitClass.retrofit.create(MyIngredientList.class);
        Log.e(TAG, "수정 목록 보내기"+getUserId());

        Call<String> call = api.getUserIngredientList(getUserId(), getAddList(), getDeleteList());
        call.enqueue(new Callback<String>()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e("onSuccess", response.body());

                    String jsonResponse = response.body();
                    try {
                        JSONObject jsonObject = new JSONObject( jsonResponse );
                        Log.e(TAG, jsonObject.toString());
                        if (jsonObject.getString("success").equals("true")) {
                            jsonArray = jsonObject.getJSONArray("result");

                            // 받아온 데이터(나의 재료 재고 목록) 전체 출력
                            Log.e(TAG, jsonArray.toString());

                            // 받아온 데이터 출력
                            String name = null;
                            String freshness = null;
                            for(int i=0; i < jsonArray.length(); i++) {
                                jsonObject = jsonArray.getJSONObject(i);
                                name = jsonObject.get("sortkey").toString();
                                freshness = jsonObject.get("freshness").toString();
                                ingredientList.add(new MyIngredient(name, freshness));
                            }
                            outputOriginalPage();
                        } else {
                            Toast.makeText( getApplicationContext(), "레시피 가져오기에 실패했습니다.", Toast.LENGTH_SHORT ).show();
                            return;
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "로그 없음");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // DB에 신선도에 대한 재료 정보가 있는지 확인
    private void checkIngredient(String ingredient) {
        // 데이터 가져오기
        RetrofitClass retrofitClass = new RetrofitClass(5000);
        MyIngredientCheck api = retrofitClass.retrofit.create(MyIngredientCheck.class);
        Log.e(TAG, ingredient);
        Call<String> call = api.getCheckIngredient(ingredient);
        call.enqueue(new Callback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("onSuccess", response.body());

                    String jsonResponse = response.body();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        Log.e(TAG, jsonObject.toString());
                        if (jsonObject.getString("success").equals("true")) {
                            ingredientList.add(new MyIngredient(ingredient, "신선"));
                            addIngredient.add(ingredient);
                            outputChangePage();
                        } else {
                            Toast.makeText(getApplicationContext(), "레시피 가져오기에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "로그 없음");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    private String getAddList() {
        if(addIngredient.size() == 0)
            return "";
        else {
            String result = "";

            for(int i=0; i < addIngredient.size(); i++) {
                result += addIngredient.get(i);

                if(i+1 != addIngredient.size()) {
                    result += " ";
                }
            }
            return result;
        }
    }

    private String getDeleteList() {
        if(deleteIngredient.size() == 0)
            return "";
        else {
            String result = "";

            for(int i=0; i < deleteIngredient.size(); i++) {
                result += deleteIngredient.get(i);

                if(i+1 != deleteIngredient.size()) {
                    result += " ";
                }
            }
            return result;
        }
    }

}
