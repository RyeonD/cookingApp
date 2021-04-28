package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

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

        // 검색 결과 페이지 상단에 주재료 보여줌
        TextView textView = findViewById(R.id.main_grocery_list);
        textView.setText(mainList);

        // 요리 리스트 출력
        cookList = findViewById(R.id.scroll_view_layout);
        try {
            getRecipeData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 뒤로 가기 버튼
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
            // json객체에서 요리 이름과 이미지 가져와 변수에 정의
            cook_name = cookObject.getString("name");
            cook_img = cookObject.getString("imagelink");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 요리 목록 출력
        // 요리 이름과 이미지 출력할 레이아웃 가져와 각각의 데이터 삽입
        // 데이터가 삽입된 레이아웃을 해당 화면 레이아웃에 추가
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

        // 요리 이미지 클릭 시 요리 정보가 담긴 json 객체를 넘겨주기 위해 Hashmap에 삽입
        imageMap.put(cntId, cookObject);
        imageView.setId(cntId++);

        cookList.addView(cookView);

        // 요리 이미지 클릭 시
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭한 요리 이미지에 맞는 정보를 Hashmap에서 id(key값)로 가져옴
                JSONObject cookInfo = imageMap.get(imageView.getId());
                intent = new Intent(getApplicationContext(), CookInfoPageActivity.class);
                Iterator iterator = cookInfo.keys();

                // 가져온 요리 정보를 다음에 올 페이지(액티비티)로 보내기 위해 intent에 넣음
                while(iterator.hasNext()) {
                    try {
                        String info = iterator.next().toString();
                        intent.putExtra(info, cookInfo.getString(info).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                startActivity(intent);
            }
        });
    }
}
