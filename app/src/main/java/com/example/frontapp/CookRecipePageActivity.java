package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Inflater;

public class CookRecipePageActivity extends AppCompatActivity {
    private static String TAG = "CookRecipePageActivity";
    GestureDetector detector;
    private Intent intent;
    private String name;
    private String recipe;
    private String recipe_imagelink;

    private LinearLayout insertRecipe;
    private ImageView imageView;
    private TextView textView;

    private ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_recipe_page);

//        getApplicationContext().startService(new Intent(CookRecipePageActivity.this, SpeechRecognitionService.class));

        scrollView = findViewById(R.id.recipe_scroll_view);

        // 데이터 받아옴
        intent = getIntent();
        getRecipe(intent);

        insertRecipe = findViewById(R.id.recipe_list_insert);
        // 받아온 데이터 출력
        outputRecipe();

        // 뒤로가기 버튼 클릭 시 동작
        findViewById(R.id.recipe_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String answer = intent.getStringExtra("action");

        if (answer.contains("뒤로가기")) {
            onBackPressed();
        }
        else if (answer.contains("스크롤다운")) {
            Log.e(TAG, "스크롤을 내립니다."+scrollView.getScrollY());
            scrollView.scrollTo(0, scrollView.getScrollY()+460);
        }
    }

    // intent 안 데이터 가져오기
    private void getRecipe(Intent intent) {
        name = intent.getStringExtra("name");
        recipe = intent.getStringExtra("recipe");
        recipe_imagelink = intent.getStringExtra("recipe_imagelink");
    }

    // 레시피 출력 - 가져온 데이터 처리
    private void outputRecipe() {
        TextView title = findViewById(R.id.recipe_cook_name);
        title.setText(name);

        // 출력하기 위해 가져온 텍스트 replace함
        recipe = recipe.replace("[\"","").replace("\"]","").replace("\\","").replace("n"," ");
        recipe_imagelink = recipe_imagelink.replaceAll("[\\\\|\\[|\\]|\"]","").replace(","," ");

        // 레시피 설명과 각각의 이미지를 배열화
        String [] recipeText = recipe.split("\",\"");
        String [] recipeImage = recipe_imagelink.split(" ");

        // 각 이미지와 레시피 텍스트 매칭
        for(int i = 0; i < recipeText.length; i++) {
            insertRecipe(recipeText[i], recipeImage[i]);
        }
    }

    // 화면에 데이터(내용) 삽입
    private void insertRecipe(String text, String image) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.cook_recipe_list, null);

        imageView = view.findViewById(R.id.recipe_image);
        textView = view.findViewById(R.id.recipe_text);

        // 이미지 삽입
        try {
            URL url = new URL(image);

            Glide.with(this).load(url).into(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        // 텍스트 삽입
        textView.setText(text);

        insertRecipe.addView(view);
    }
}
