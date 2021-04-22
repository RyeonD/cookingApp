package com.example.frontapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity: ";
    static final int PERMISSIONS_REQUEST_CODE = 1001;
    private String[] PERMISSIONS  = {Manifest.permission.CAMERA};
    static final int REQUEST_CAMERA = 1;
    final static int TAKE_PICTURE = 1;
    final static int REQUEST_TAKE_PHOTO = 1;
    TextView mainText;
    GridLayout cookList;
    Intent intent;

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

        permissionCheck();

        // search button click 동작
        findViewById(R.id.image_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, TAKE_PICTURE);
                }
            }
        });

        // person info button click 동작
        findViewById(R.id.info_page_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), PersonInfoActivity.class);
                startActivity(intent);
            }
        });
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

    // 권한 확인
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

    // 촬영한 사진 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO) {
            Bundle imageBundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) imageBundle.get("data");

            intent = new Intent(getApplicationContext(), PhotoCheckActivity.class);
            intent.putExtra("img", imageBitmap);
            startActivity(intent);
        }
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

    public String todayDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

        return dateFormat.format(date);
    }
}