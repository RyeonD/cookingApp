package com.example.frontapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.gridlayout.widget.GridLayout;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    TextView mainText;
    GridLayout cookList;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainText = findViewById(R.id.main_text1);
        cookList = findViewById(R.id.cook_list);
        try {
            getRecipeData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // person info button click 동작
        Button infoBtn = findViewById(R.id.info_page_btn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "info 클릭됨",Toast.LENGTH_LONG).show();
            }
        });

        // search button click 동작
        Button searchBtn = findViewById(R.id.image_search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    // 레시피 가져와 파싱
    private void getRecipeData() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/감자전.json";

        // 파일 가져오기
        try {
            InputStream data = assetManager.open("jsons/감자전.json");
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
                JSONObject recipe = jsonObject.getJSONObject(i.next().toString());
                String cook_name = recipe.getString("name");
                String cook_img = recipe.getString("imagelink");
                cookAdd(cook_name, cook_img);
            }

        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // 요리 목록 출력
    public void cookAdd(String cook_name, String cook_img) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cookView = inflater.inflate(R.layout.cook_info, null);
        TextView name = cookView.findViewById(R.id.cook_name);
        ImageView imageView = cookView.findViewById(R.id.imageButton);
        name.setText(cook_name);

        try {
            URL url = new URL(cook_img);

            // Bitmap 생성
//            URLConnection conn = (URLConnection) img_url.openConnection();
//            conn.setDoInput(true);
//            conn.connect();
//            InputStream is = conn.getInputStream();

//            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            imageView.setImageBitmap(bitmap);
            Glide.with(this).load(url).into(imageView);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        cookList.addView(cookView);
    }
}