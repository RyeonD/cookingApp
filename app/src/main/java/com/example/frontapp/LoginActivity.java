package com.example.frontapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = "LoginActivity: ";
    private static final String PREF_USER_ID = "MyAutoLogin";
    private EditText login_id, login_password;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
//    public static class AutoLogin {
//
//        private static final String PREF_USER_ID = "MyAutoLogin";
//
//        // 모든 엑티비티에서 인스턴스를 얻기위함
//        static SharedPreferences getSharedPreferences(Context ctx) {
//            // return PreferenceManager.getDefaultSharedPreferences(ctx);
//            return ctx.getSharedPreferences(PREF_USER_ID, Context.MODE_PRIVATE);
//        }
//
//        // 계정 정보 저장 : 로그인 시 자동 로그인 여부에 따라 호출 될 메소드, 해당코드는  userId가 저장된다.
//        public static void setUserId(Context ctx, String userId, boolean auto) {
//            SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
//            editor.putString(PREF_USER_ID, userId);
//            editor.putBoolean("auto", auto);
//            editor.commit();
//        }
//
//        // 저장된 정보 가져오기 : 현재 저장된 정보를 가져오기 위한 메소드
//        public static String getUserId(Context ctx) {
//            return getSharedPreferences(ctx).getString(PREF_USER_ID, "");
//        }
//
//        // 저장된 정보 가져오기 : 현재 저장된 정보를 가져오기 위한 메소드
//        public static boolean getAuto(Context ctx) {
//            return getSharedPreferences(ctx).getBoolean("auto", false);
//        }
//
//        // 로그아웃 : 자동 로그인 해제 및 로그아웃 시 호출 될 메소드
//        public static void clearUserId(Context ctx) {
//            SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
//            editor.clear();
//            editor.commit();
//        }
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.activity_login);

        login_id = findViewById( R.id.login_id );
        login_password = findViewById( R.id.login_password );

        sharedPreferences = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 회원 가입 버튼 클릭
        findViewById( R.id.join_button ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( LoginActivity.this, RegisterActivity.class );
                startActivity( intent );
            }
        });

        //로그인 버튼 클릭
        findViewById( R.id.login_button ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserId = login_id.getText().toString();
                String UserPwd = login_password.getText().toString();

                // 입력된 ID와 PASSWORD 를 서버로 넘겨줘서 결과 반환 받음
                RetrofitClass retrofitClass = new RetrofitClass();
                LoginInterface api = retrofitClass.retrofit.create(LoginInterface.class);
                Call<String> call = api.getUserLogin(UserId, UserPwd);
                call.enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                    {
                        // 로그인에 성공하면 실행
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.e("onSuccess", response.body());

                            String jsonResponse = response.body();
                            parseLoginData(jsonResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                    {
                        Log.e(TAG, "에러 = " + t.getMessage());
                    }
                });
            }
        });
    }

    private void parseLoginData(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("success").equals("true"))
            {
//                saveInfo(response);
                String UserId = jsonObject.getString( "user_id" );
                String UserName = jsonObject.getString( "user_name" );

                // sharedPreference 에 저장
                editor.putString("UserId", UserId);
                editor.putString("UserName", UserName);

                // 자동 로그인을 위해 ID 저장
                autoLogin(UserId);

                Toast.makeText( getApplicationContext(), String.format("%s님 환영합니다.", UserName), Toast.LENGTH_SHORT ).show();
                Intent intent = new Intent( LoginActivity.this, MainActivity.class );

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity( intent );
                finish();

            } else {//로그인 실패시
                Toast.makeText( getApplicationContext(), "로그인에 실패하셨습니다.", Toast.LENGTH_SHORT ).show();
                return;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

    private void autoLogin(String UserId) {
        CheckBox checkBox = findViewById(R.id.auto_login);

        // 자동 로그인
        if(checkBox.isChecked()) {
            Log.e(TAG, "auto login succese");
            editor.putBoolean("autoLogin", true);
        }
        else {
            Log.e(TAG, "auto login fail");
            editor.putBoolean("autoLogin", false);
        }

        editor.commit();
    }
}