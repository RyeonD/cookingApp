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
    private SharedPreferences sharedPreferencesUser;
    private static final String PREF_USER_ID = "MyAutoLogin";
    private static final String PREF_USER_INGREDIENT = "MyIngredientList";
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로그인 확인 후 결과에 따른 페이지 실행
        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        userId = sharedPreferencesUser.getString("UserId","");
        Log.e(TAG, userId);
        if(userId != "") {
            setPageLoginSuccess();
        }
        else {
            setPageLoginFail();
        }
    }

    // 로그인 되어 있을 때 페이지 출력
    private void setPageLoginSuccess() {
        setContentView(R.layout.activity_person_info);

        // 페이지 출력 지정
        setTextView();

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
                settingButtonClick();
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
    public void setTextView() {
        user_image = findViewById(R.id.user_info_image);
        user_name = findViewById(R.id.user_name);
        user_id = findViewById(R.id.user_id);
        keep_grocery = findViewById(R.id.grocery_keep);
        expiration_date = findViewById(R.id.expiration_date);
        allergy = findViewById(R.id.allergy);
        disease = findViewById(R.id.disease);

        // 페이지 입력
        user_image.setImageResource(R.drawable.profile_1);

        user_name.setText(sharedPreferencesUser.getString("UserName",""));
        user_id.setText(userId);

        SharedPreferences sharedPreferencesUserIngredient = getSharedPreferences(PREF_USER_INGREDIENT, MODE_PRIVATE);
        keep_grocery.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("ingredientCountSum", 0)));
        expiration_date.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("freshLevel3", 0)));

        allergy.setText("3");
        disease.setText("2");
    }

    // 설정 페이지 이동 버튼
    private void settingButtonClick() {
        Log.e(TAG, "설정 페이지 확인");
        intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }

    // 로그인이 되어 있지 않을 때 페이지 출력
    private void setPageLoginFail() {
        setContentView(R.layout.activity_person_info_login_fail);

        findViewById(R.id.no_user_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.setting_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingButtonClick();
            }
        });
    }
}
