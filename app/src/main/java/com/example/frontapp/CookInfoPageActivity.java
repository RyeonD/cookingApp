package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.ContentView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookInfoPageActivity extends AppCompatActivity {
    private static String TAG = "AppCompatActivity";

    private Intent intent;
    private String name;
    private String imagelink;
    private String ingredient;
    private String recipe;
    private String recipe_imagelink;
    private String youtubelink;
    private LinearLayout videoLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_info_page);

        Log.e(TAG, "성공");
        intent = getIntent();

        // 요리 정보들 가져옴
        getCookInfo(intent);

        // 요리 정보 출력
        outputCookInfo();

        // 비디오 출력
        videoLinearLayout = findViewById(R.id.video_linearlayout);
        String [] videoUrlList = youtubelink.replaceAll("[\\\\|\\[|\\]|\"]","").replace(","," ").split(" ");
        for(String url : videoUrlList)
            outputVideo(url);

        findViewById(R.id.recipe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), CookRecipePageActivity.class);

                intent.putExtra("name", name);
                intent.putExtra("recipe", recipe);
                intent.putExtra("recipe_imagelink", recipe_imagelink);
                startActivity(intent);
            }
        });

        // 뒤로 가기 버튼 동작
        findViewById(R.id.cook_info_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 검색 버튼 동작
        findViewById(R.id.cook_info_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "검색 버튼을 다른 걸로 바꿀 수 있도록 해보자", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // intent로 데이터 넘겨 받아온 것 변수에 정의
    private void getCookInfo(Intent intent) {
        name = intent.getStringExtra("name");
        imagelink = intent.getStringExtra("imagelink");
        ingredient = intent.getStringExtra("ingredient");
        youtubelink = intent.getStringExtra("youtubelink");

        // 레시피 페이지로 내보낼 데이터
        recipe = intent.getStringExtra("recipe");
        recipe_imagelink = intent.getStringExtra("recipe_imagelink");
    }

    private void outputCookInfo() {
        // 이미지 삽입
        addCookImage();

        // 요리 이름 및 재료 삽입
        addCookText();
    }

    // 이미지 삽입
    private void addCookImage() {

        ImageView imageView = findViewById(R.id.cook_image);
        try {
            URL url = new URL(imagelink);       // url 이미지 가져옴
            Glide.with(this).load(url).into(imageView);     // 가져와서 이미지 뷰에 추가
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);     // 이미지 뷰 크기에 맞춰 이미지 확대
    }

    // 요리 이름 및 재료 추가
    private void addCookText() {
        TextView cookName = findViewById(R.id.cook_name);
        TextView groceries = findViewById(R.id.groceries_text);

        // 받아온 파일 안 재료 부분에 있는 백 슬래시, 대괄호 지우고, ,뒤에 띄어쓰기
        ingredient = ingredient.replaceAll("[\\\\|\"|\\[|\\]]","").replace(",",", ");

        cookName.setText(name);
        groceries.setText(ingredient);
    }

    // 비디오 출력
    private void outputVideo(String url) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View videoLayoutView = layoutInflater.inflate(R.layout.video_layout, null, false);
        ImageView imageView = videoLayoutView.findViewById(R.id.video_image);

        videoLinearLayout.addView(videoLayoutView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }
}
