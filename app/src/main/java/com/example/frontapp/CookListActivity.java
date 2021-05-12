package com.example.frontapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CookListActivity extends AppCompatActivity {
    private static String TAG = "CookListActivity";
    private LinearLayout cookList;
    private Intent intent;
    private int cntId = 100;
    private JSONArray jsonArray;
    HashMap <Integer, JSONObject> imageMap = new HashMap<Integer, JSONObject>();
    String cook_name;
    String cook_img;
    String cook_ingredients;
    String[] ingredientList;
    private CheckTypesTask task;
    private static final String PREF_USER_ID = "MyAutoLogin";
    SharedPreferences sharedPreferencesUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_list);

        intent = getIntent();
        ingredientList = intent.getStringArrayExtra("ingredientList");
        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);

        // 검색 결과 페이지 상단에 주재료 보여줌
        TextView textView = findViewById(R.id.main_grocery_list);
        textView.setText(ingredientList[1]);

        // 요리 리스트 출력
        cookList = findViewById(R.id.scroll_view_layout);

        task = new CheckTypesTask();
        task.execute();

        // Local 추천 요리 가져오기
//        try {
//            getLocalCookList();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 서버에서 추천 요리 받아오기
        getCookList();
    }

    private class CheckTypesTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(CookListActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("선택된 재료들로 요리를 검색중입니다.");

            // show dialog
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                while(true) {
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    // 파일 가져오기 - 서버 연결 안 되어있을 때
    private void getLocalCookList() throws IOException {

        AssetManager assetManager = getAssets();
        try {
            InputStream data = assetManager.open("jsons/cookList.json");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 서버에서 추천 요리 리스트 가져오기
    private void getCookList() {
        RetrofitClass retrofitClass = new RetrofitClass(5001);
        CookListInterface api = retrofitClass.retrofit.create(CookListInterface.class);
        Call<String> call = api.getRecipe(sharedPreferencesUser.getString("UserId", ""), ingredientList[0], ingredientList[1]);
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

                        if (jsonObject.getString("success").equals("true")) {
                            task.onPostExecute("종료");
                            jsonArray = jsonObject.getJSONArray("recipe_list");
                            // 요리 리스트 출력
                            cookList = findViewById(R.id.scroll_view_layout);
                            getCookData();
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

        // 뒤로 가기 버튼
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // 레시피 가져와 파싱
    private void getCookData() {
        try {
            // json 객체 생성 및 파싱
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject cook = (JSONObject) jsonArray.get(i);
                cookAdd(cook);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 요리 목록 출력
    public void cookAdd(JSONObject cookObject) throws JSONException {
        try {
            // json객체에서 요리 이름과 이미지 가져와 변수에 정의
            cook_name = cookObject.getString("name");
            cook_img = cookObject.getString("imagelink");
            cook_ingredients = cookObject.getString("ingredient");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 요리 목록 출력
        // 요리 이름과 이미지 출력할 레이아웃 가져와 각각의 데이터 삽입
        // 데이터가 삽입된 레이아웃을 해당 화면 레이아웃에 추가
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cookView = inflater.inflate(R.layout.cook_info, null);
        TextView mainGrocery = cookView.findViewById(R.id.main_grocery_in_cook_list);
        TextView name = cookView.findViewById(R.id.join_title);
        ImageView imageView = cookView.findViewById(R.id.imageView);

        String input = new String();
        for(String s : ingredientList[1].split(" ")) {
            if (cook_ingredients.contains(s)) {
                input += (s + " ");
            }
        }

        if(!input.isEmpty()) {
            mainGrocery.setText(input.substring(0, input.length()-1));
        }
        else
            mainGrocery.setVisibility(View.GONE);

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
