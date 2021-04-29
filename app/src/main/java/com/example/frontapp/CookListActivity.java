package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CookListActivity extends AppCompatActivity {
    private static String TAG = "CookListActivity";
    private LinearLayout cookList;
    private Intent intent;
    private int cntId = 100;
    private JSONArray jsonArray;
    HashMap <Integer, JSONObject> imageMap = new HashMap<Integer, JSONObject>();
    String cook_name;
    String cook_img;
    String cook_ingredients;
    String cook_recipes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_list);

        intent = getIntent();
        String[] ingredientList = intent.getStringArrayExtra("ingredientList");
        Log.e(TAG, "ingredientList");

        // 검색 결과 페이지 상단에 주재료 보여줌
        TextView textView = findViewById(R.id.main_grocery_list);
        textView.setText(ingredientList[1]);

        RetrofitClass retrofitClass = new RetrofitClass();
        CookListInterface api = retrofitClass.retrofit.create(CookListInterface.class);
        Call<String> call = api.getRecipe(ingredientList[0], ingredientList[1]);
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

                        if (jsonObject.getString("success").equals("true")) {
                            jsonArray = jsonObject.getJSONArray("recipe_list");
                            // 요리 리스트 출력
                            cookList = findViewById(R.id.scroll_view_layout);
                            try {
                                getRecipeData();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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



        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // 레시피 가져와 파싱
    private void getRecipeData() throws IOException {
//        AssetManager assetManager = getAssets();
//        String filename = "jsons/gamjajeon.json";
//
//        // 파일 가져오기
//        try {
//            InputStream data = assetManager.open("jsons/gamjajeon.json");
//            InputStreamReader dataReader = new InputStreamReader(data);
//            BufferedReader reader = new BufferedReader(dataReader);
//
//            StringBuffer buffer = new StringBuffer();
//            String line = reader.readLine();
//            while (line != null) {
//                buffer.append(line + "\n");
//                line = reader.readLine();
//            }
        try {
            // json 객체 생성 및 파싱

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject cook = (JSONObject) jsonArray.get(i);
                cookAdd(cook);
            }
//            while(i.hasNext()){
//                JSONObject cook = jsonObject.getJSONObject(i.next().toString());
//                cookAdd(cook);
//            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 요리 목록 출력
    public void cookAdd(JSONObject cookObject) throws JSONException {
        try {
            cook_name = cookObject.getString("name");
            cook_img = cookObject.getString("imagelink");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cookView = inflater.inflate(R.layout.cook_info, null);
        TextView name = cookView.findViewById(R.id.textView);
        ImageView imageView = cookView.findViewById(R.id.imageView);
        name.setText(cook_name);

        try {
            URL url = new URL(cook_img);

            Glide.with(this).load(url).into(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        imageMap.put(cntId, cookObject);
        imageView.setId(cntId++);

        cookList.addView(cookView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject cookInfo = imageMap.get(imageView.getId());
                intent = new Intent(getApplicationContext(), CookInfoNGredient.class);
                try {
                    intent.putExtra("name", cookInfo.getString("name").toString());
                    intent.putExtra("img", cookInfo.getString("img").toString());
//                    intent.putExtra("ingredient", cookInfo.getString("ingredient").toString());
//                    intent.putExtra("recipe", cookInfo.getString("recipe").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
    }
}
