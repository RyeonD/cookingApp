package com.example.frontapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static String TAG = "RegisterActivity";
    private EditText join_email, join_password, join_name, join_pwck, join_address, join_phone, join_id;
    private Button join_button, check_button, delete_button, radioGender;
    private AlertDialog dialog;
    private boolean validate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_join );

        //아이디값 찾아주기
        join_email = findViewById( R.id.join_email );
        join_password = findViewById( R.id.join_password );
        join_name = findViewById( R.id.join_name );
        join_pwck = findViewById(R.id.join_pwck);
        join_address = findViewById((R.id.join_address));
        join_phone = findViewById((R.id.join_phone));
        join_id = findViewById((R.id.join_id));

        //아이디 중복 체크
        check_button = findViewById(R.id.check_button);
        check_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String UserId = join_id.getText().toString();
                if (validate) {
                    return; //검증 완료
                }

                if (UserId.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                    dialog.show();
                    return;
                }

                RetrofitClass retrofitClass = new RetrofitClass(5002);
                ValidateInterface api = retrofitClass.retrofit.create(ValidateInterface.class);
                Call<String> call = api.getUserValidate(UserId);
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
                                //회원가입 성공시
                                if (jsonObject.getString("success").equals("true")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    dialog = builder.setMessage("사용할 수 있는 아이디입니다.").setPositiveButton("확인", null).create();
                                    dialog.show();
                                    join_id.setEnabled(false); //아이디값 고정
                                    validate = true; //검증 완료
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    dialog = builder.setMessage("이미 존재하는 아이디입니다.").setNegativeButton("확인", null).create();
                                    dialog.show();
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
        });

        RadioGroup genderType = findViewById(R.id.radioGroup);

        //라디오버튼 체크시 이벤트
        genderType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.radioMale) {
                    radioGender = findViewById(R.id.radioMale);
                }
                else if(checkedId == R.id.radioFemale) {
                    radioGender = findViewById(R.id.radioFemale);
                }
            }
        });

        //회원가입 버튼 클릭 시 수행
        join_button = findViewById( R.id.join_button );
        join_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "버튼 눌림");

                final String UserEmail = join_email.getText().toString();
                final String UserPwd = join_password.getText().toString();
                final String UserName = join_name.getText().toString();
                final String PassCk = join_pwck.getText().toString();
                final String Address = join_address.getText().toString();
                final String Phone = join_phone.getText().toString();
                final String UserId = join_id.getText().toString();
                final String Gender = radioGender.getText().toString();

                Map<String, String> info_map = new HashMap<>();
                info_map.put("phone", Phone);
                info_map.put("email", UserEmail);
                info_map.put("address", Address);
                info_map.put("gender", Gender);
                JSONObject UserInfo = new JSONObject(info_map);

                //아이디 중복체크 했는지 확인
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("중복된 아이디가 있는지 확인하세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                //한 칸이라도 입력 안했을 경우
                if (UserEmail.equals("") || UserPwd.equals("") || UserName.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("모두 입력해주세요.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                RetrofitClass retrofitClass = new RetrofitClass();
                RegisterInterface api = retrofitClass.retrofit.create(RegisterInterface.class);
                Call<String> call = api.getUserRegist(UserId, UserPwd, UserName, UserInfo);
                call.enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.e("onSuccess", response.body());

                            String jsonResponse = response.body();
                            parseRegisterData(jsonResponse, UserPwd, PassCk, UserName);
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
        delete_button = findViewById( R.id.delete );
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });
    }
    private void parseRegisterData(String response,  String UserPwd, String PassCk, String UserName)
    {
        try {
            JSONObject jsonObject = new JSONObject( response );
            boolean success = jsonObject.getBoolean( "success" );
            //회원가입 성공시
            if(UserPwd.equals(PassCk)) {
                if (jsonObject.getString("success").equals("true")) {
                    Log.e(TAG, "회원가입 성공");
                    Toast.makeText(getApplicationContext(), String.format("%s님 가입을 환영합니다.", UserName), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);

                    //회원가입 실패시
                } else {
                    Log.e(TAG, "회원가입 실패");
                    Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                dialog = builder.setMessage("비밀번호가 동일하지 않습니다.").setNegativeButton("확인", null).create();
                dialog.show();
                return;
            }

        } catch (JSONException e) {
            Log.e(TAG, "로그 없음");
            e.printStackTrace();
        }

    }
}