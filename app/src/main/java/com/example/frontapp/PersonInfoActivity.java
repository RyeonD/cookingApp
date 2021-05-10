package com.example.frontapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String PREF_USER_ID = "MyAutoLogin";
    private static final String PREF_USER_INGREDIENT = "MyIngredientList";

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

        // 설정 페이지로
        findViewById(R.id.setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "설정 페이지 확인");
                intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        // 재료 관리 페이지로
        findViewById(R.id.grocery_manage_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), MyIngredientListActivity.class);
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
        user_image.setImageResource(R.drawable.profile_1);

        SharedPreferences sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        user_name.setText(sharedPreferencesUser.getString("UserName",""));
        user_id.setText(sharedPreferencesUser.getString("UserId",""));

        SharedPreferences sharedPreferencesUserIngredient = getSharedPreferences(PREF_USER_INGREDIENT, MODE_PRIVATE);
        keep_grocery.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("ingredientCountSum", 0)));
        expiration_date.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("freshLevel3", 0)));

        allergy.setText("3");
        disease.setText("2");
    }
}
