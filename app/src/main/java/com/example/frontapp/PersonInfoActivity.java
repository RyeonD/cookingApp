package com.example.frontapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PersonInfoActivity extends AppCompatActivity {
    private final static String TAG = "PersonInfoActivity: ";
    Intent intent;
    ImageView user_image;
    TextView user_name;
    TextView user_id;
    TextView keep_grocery;
    TextView expiration_date;
    TextView allergy;
    TextView disease;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        // 페이지 출력 지정
        setPage();

        // 메인 페이지로
        findViewById(R.id.mainpage_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // 검색 페이지로
        findViewById(R.id.info_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                intent = new Intent(getApplicationContext(), CameraActivity.class);
//                startActivity(intent);
            }
        });

        // 고객 지원 페이지로
        findViewById(R.id.cs_page_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "고객 지원 페이지 확인");
            }
        });

        // 설정 페이지로
        findViewById(R.id.setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "설정 페이지 확인");
            }
        });

        // 재료 관리 페이지로
        findViewById(R.id.grocery_manage_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), GroceryManagementActivity.class);
                startActivity(intent);
            }
        });

        // 건강 관리 페이지로 - 사용 안함
        findViewById(R.id.health_manage_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // 페이지 입력 값 디비에서 가져와 반환
    public void setPage() {
        user_image = findViewById(R.id.user_info_image);
        user_name = findViewById(R.id.user_name);
        user_id = findViewById(R.id.user_id);
        keep_grocery = findViewById(R.id.grocery_keep);
        expiration_date = findViewById(R.id.expiration_date);
        allergy = findViewById(R.id.allergy);
        disease = findViewById(R.id.disease);

        // 페이지 입력
        user_image.setImageResource(R.drawable.test_img);
        user_name.setText("나요비");
        user_id.setText("yobi@kmr.com");
        keep_grocery.setText("6");
        expiration_date.setText("2");
        allergy.setText("3");
        disease.setText("2");
    }
}
