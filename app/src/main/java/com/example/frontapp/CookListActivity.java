package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
        String mainList = intent.getStringExtra("mainList");
        Log.e(TAG, "mainlist");

        // 검색 결과 페이지 상단에 주재료 보여줌
        TextView textView = findViewById(R.id.main_grocery_list);
        textView.setText(mainList);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CookListInterface.REGIST_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();
        CookListInterface api = retrofit.create(CookListInterface.class);
        Call<String> call = api.getRecipe(mainList);
        call.enqueue(new Callback<String>()
        {
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
                            System.out.println(jsonObject.toString());
                        } else {

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

        // 요리 리스트 출력
        cookList = findViewById(R.id.scroll_view_layout);
        try {
            getRecipeData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // 레시피 가져와 파싱
    private void getRecipeData() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/감자전.json";

        // 파일 가져오기
        try {
            InputStream data = assetManager.open("jsons/감자전.json");
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
            Iterator i = jsonObject.keys();
            while(i.hasNext()){
                JSONObject cook = jsonObject.getJSONObject(i.next().toString());
                cookAdd(cook);
            }

        }
        catch (IOException | JSONException e) {
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
