package com.example.frontapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity: ";

    static final int PERMISSIONS_REQUEST_CODE = 1001;
    private String[] PERMISSIONS  = {Manifest.permission.CAMERA};
    static final int REQUEST_CAMERA = 1;
    final static int TAKE_PICTURE = 1;
    final static int REQUEST_TAKE_PHOTO = 1;

    private Intent intent;
    private Button loginBtn;
    private JSONArray jsonArray;

    private static final String PREF_USER_ID = "MyAutoLogin";
    SharedPreferences sharedPreferencesUser;
    private boolean autoLoginCheck;

    private static final String PREF_USER__INGREDIENT = "MyIngredientList";
    SharedPreferences sharedPreferencesUserIngredient;

    private long backKeyPressedTime = 0;
    private Toast toast;

    private int fresh_first;    // 신선
    private int fresh_second;   // 양호
    private int fresh_third;   // 위험
    private int fresh_fourth;   // 만료

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카메라 권한 확인 및 권한 부여
        permissionCheck();

        // 다른 페이지에서 카메라 재실행 시
        intent = getIntent();
        if(intent.getBooleanExtra("camera", false))
            startCamera();

        // search button click 동작 - 카메라 실행
        findViewById(R.id.image_search_btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startCamera();
            }
        });

        // person info button click 동작 - 수정 필요
        findViewById(R.id.info_page_btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                intent = new Intent(getApplicationContext(), PersonInfoActivity.class);
                intent = new Intent(getApplicationContext(), PersonInfoActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 자동 로그인 확인
        loginBtn = findViewById(R.id.login_btn);
        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        fresh_first = 0;
        fresh_second = 0;
        fresh_third = 0;
        fresh_fourth = 0;
        loginCheck();

        // login button click 동작 - 로그인 페이지로
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginBtn.getText().toString().contains("로그인")) {
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    intent = new Intent(getApplicationContext(), MyIngredientListActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    // 자동 로그인 확인
    private void loginCheck(){
        String UserId = sharedPreferencesUser.getString("UserId", "");
        autoLoginCheck = sharedPreferencesUser.getBoolean("autoLogin", false);

        Log.e(TAG, UserId);
        if(UserId.length() == 0) {
            setCircleText(false);
        }
        else {
            setPesonalGrocery(UserId);
        }
    }

    // 서버에서 나의 재료 재고 목록 가져오기

    private void setPesonalGrocery(String UserId) {
        // 데이터 가져오기
        RetrofitClass retrofitClass = new RetrofitClass(5000);
        MainInterface api = retrofitClass.retrofit.create(MainInterface.class);
        Call<String> call = api.getUserId(UserId);
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

                            sharedPreferencesUserIngredient = getSharedPreferences(PREF_USER__INGREDIENT, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferencesUserIngredient.edit();
                            editor.putString("ingredientList", jsonArray.toString());
                            editor.commit();

                            // 받아온 데이터(나의 재료 재고 목록) 전체 출력
                            Log.e(TAG, jsonArray.toString());
                            // 받아온 데이터 출력
                            getMyGroceryList();
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

    // 재료 재고 목록 가져와 파싱 - 수정 필요
    private void getMyGroceryList() {
        try {
            // json 객체 생성 및 파싱
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject cook = (JSONObject) jsonArray.get(i);
                switch (cook.getString("freshness")) {
                    case "신선": fresh_first++; break;
                    case "양호": fresh_second++; break;
                    case "위험": fresh_third++; break;
                    default: fresh_fourth++; break;
                }
            }
            setCircleText(true);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 메인 화면 재료 현황
    private void setCircleText(boolean checkLogin) {
        ConstraintLayout textInCircle = findViewById(R.id.change_circle_text);
        LayoutInflater inflater = (LayoutInflater) getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
        if(checkLogin) {
            // 로그인 되면
            loginBtn.setText("나의 재료 보러가기");

            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.main_circle_login_complete_layout, null, false);
            TextView first = linearLayout.findViewById(R.id.level1);
            TextView second = linearLayout.findViewById(R.id.level2);
            TextView third = linearLayout.findViewById(R.id.level3);
            TextView fourth = linearLayout.findViewById(R.id.level4);

            first.setText(String.format("신선 - %d", fresh_first));
            second.setText(String.format("양호 - %d", fresh_second));
            third.setText(String.format("위험 - %d", fresh_third));
            fourth.setText(String.format("만료 - %d", fresh_fourth));

            textInCircle.addView(linearLayout);
        }
        else {
            loginBtn.setText("로그인");
            ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.main_circle_request_login_layout, null, false);
            textInCircle.addView(constraintLayout);
        }
    }

    // 뒤로가기 설정(한번은 알림, 두번은 종료)
    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            autoLoginCheck();
            finish();
            toast.cancel();
        }
    }

    // 자동 로그인 미체크 시 로그아웃
    public void autoLoginCheck() {
        if(!autoLoginCheck) {
            SharedPreferences.Editor editor = sharedPreferencesUser.edit();
            editor.clear();
            editor.commit();
        }
    }

    // 카메라 실행
    public void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, TAKE_PICTURE);
        }
    }

    // 권한 확인 및 권한 부여
    private void permissionCheck() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        // Pakage.PERMISSION_GRANTED -> 권한 있음
        // Pakage.PERMISSION_DENIED -> 권한 없음
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 권한 이미 거절 - ActivityCompat.shouldShowRequestPermissionRationale()가 true 반환
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            else {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.CAMERA}, 1000);
                }
            }
        }
    }

    // 사용자가 부여한 권한 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if(grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    // 해결 필요
                    if(!cameraPermission) {
                        Toast.makeText(this, "권한 허가가 필요함", Toast.LENGTH_SHORT).show();
                        Log.e("권한 확인", "권한 허가 필요");
                    }
                    else {
                        Log.e("권한 확인", "권한 허가되어 있음");
                        finish();
                    }
                }
        }
    }

    // 촬영한 사진 가져와 다음에 올 페이지(액티비티)로 전달
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO) {
            Bundle imageBundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) imageBundle.get("data");

            intent = new Intent(getApplicationContext(), GroceryListInPhotoActivity.class);
            intent.putExtra( "img", imageBitmap);
            startActivity(intent);
        }
    }

    public String todayDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

        return dateFormat.format(date);
    }
}