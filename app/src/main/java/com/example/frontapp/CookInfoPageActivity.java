package com.example.frontapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ContentView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class CookInfoPageActivity extends AppCompatActivity {
    private static String TAG = "AppCompatActivity";
    GestureDetector detector;

    private Intent intent;
    private String name;
    private String imagelink;
    private String ingredient;
    private String recipe;
    private String recipe_imagelink;
    private String youtubelink;
    private ViewPager2 cook_info_viewpager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_info_page);

        Log.e(TAG, "성공");
        intent = getIntent();

        // 요리 정보들 가져
        getCookInfo(intent);

        // 요리 정보 출력
        outputCookInfo();

        // 화면 전환
        detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.e(TAG, Float.toString(distanceX)+" / "+Float.toString(distanceY));
                if(distanceX > 0) {
                    intent = new Intent(getApplicationContext(), CookRecipePageActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
                }

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

        View view = findViewById(R.id.linearLayout9);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });
    }

    private void getCookInfo(Intent intent) {
        name = intent.getStringExtra("name");
        imagelink = intent.getStringExtra("imagelink");
        ingredient = intent.getStringExtra("ingredient");
        recipe = intent.getStringExtra("recipe");
        recipe_imagelink = intent.getStringExtra("recipe_imagelink");
        youtubelink = intent.getStringExtra("youtubelink");
    }

    private void outputCookInfo() {
        cook_info_viewpager = findViewById(R.id.cook_info_viewpager);

    }
}
