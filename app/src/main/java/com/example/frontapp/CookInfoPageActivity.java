package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class CookInfoPageActivity extends AppCompatActivity {
    private static String TAG = "AppCompatActivity";

    private Intent intent;
    private String name;
    private String imagelink;
    private String ingredient;
    private String recipe;
    private String recipe_imagelink;
    private String youtubelink;
    private String youtubeimage;
    private LinearLayout videoLinearLayout;
    private ScrollView scrollView;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_info_page);

        getApplicationContext().startService(new Intent(CookInfoPageActivity.this, SpeechRecognitionService.class));

        scrollView = findViewById(R.id.info_scroll_view);

        intent = getIntent();

        // 요리 정보들 가져옴
        getCookInfo(intent);

        // 요리 정보 출력
        outputCookInfo();

//        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if(status != android.speech.tts.TextToSpeech.ERROR) {
//                    textToSpeech.setLanguage(Locale.KOREAN);
//                }
//            }
//        });

        findViewById(R.id.recipe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), CookRecipePageActivity.class);

                intent.putExtra("name", name);
                intent.putExtra("recipe", recipe);
                intent.putExtra("recipe_imagelink", recipe_imagelink);
                startActivity(intent);
            }
        });

        // 뒤로 가기 버튼 동작
        findViewById(R.id.cook_info_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechRecognitionService.endYobi();
                onBackPressed();
            }
        });

        // 메인 페이지로
        findViewById(R.id.cook_info_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeechRecognitionService.onDestory();
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String answer = intent.getStringExtra("action");

        if(answer.contains("성공")) {
            Log.e(TAG, answer);
            Toast.makeText(getApplicationContext(), "안녕하세요. 말씀해주세요.", Toast.LENGTH_LONG).show();

            getApplicationContext().stopService(new Intent(CookInfoPageActivity.this, SpeechRecognitionService.class));
//            getApplicationContext().startService(new Intent(CookInfoPageActivity.this, SpeechRecognitionService.class));
        }
        else if(answer.contains("레시피")) {
            Log.e(TAG, answer);
            intent = new Intent(getApplicationContext(), CookRecipePageActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("recipe", recipe);
            intent.putExtra("recipe_imagelink", recipe_imagelink);
            startActivity(intent);
        }
        else if (answer.contains("스크롤다운")) {
            Log.e(TAG, "스크롤을 내립니다."+scrollView.getScrollY());
            scrollView.scrollTo(0, scrollView.getBottom());
        }

        super.onNewIntent(intent);
    }

    // intent로 데이터 넘겨 받아온 것 변수에 정의
    private void getCookInfo(Intent intent) {
        name = intent.getStringExtra("name");
        imagelink = intent.getStringExtra("imagelink");
        ingredient = intent.getStringExtra("ingredient");
        youtubelink = intent.getStringExtra("youtubelink");
        youtubeimage = intent.getStringExtra("youtubeimage");

        // 레시피 페이지로 내보낼 데이터
        recipe = intent.getStringExtra("recipe");
        recipe_imagelink = intent.getStringExtra("recipe_imagelink");
    }

    private void outputCookInfo() {
        // 이미지 삽입
        addCookImage();

        // 요리 이름 및 재료 삽입
        addCookText();

        // 비디오
        videoLinearLayout = findViewById(R.id.video_linearlayout);
        String [] videoLinkList = youtubelink.replaceAll("[\\\\|\\[|\\]|\"]","").split(",");
        String [] videoImageLinkList = youtubeimage.replaceAll("[\\\\|\\[|\\]|\"]","").split(",");

        for(int i = 0; i < videoLinkList.length; i++) {
            addVideo(videoLinkList[i], videoImageLinkList[i]);
        }
    }

    // 이미지 삽입
    private void addCookImage() {

        ImageView imageView = findViewById(R.id.cook_image);
        try {
            URL url = new URL(imagelink);       // url 이미지 가져옴
            Glide.with(this).load(url).into(imageView);     // 가져와서 이미지 뷰에 추가
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);     // 이미지 뷰 크기에 맞춰 이미지 확대
    }

    // 요리 이름 및 재료 추가
    private void addCookText() {
        TextView cookName = findViewById(R.id.cook_name);
        TextView groceries = findViewById(R.id.groceries_text);

        // 받아온 파일 안 재료 부분에 있는 백 슬래시, 대괄호 지우고, ,뒤에 띄어쓰기
        ingredient = ingredient.replaceAll("[\\\\|\"|\\[|\\]]","").replace(",",", ");

        cookName.setText(name);
        groceries.setText(ingredient);
    }

    // 비디오 출력
    private void addVideo(String videoUrl, String imageUrl) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View videoLayoutView = layoutInflater.inflate(R.layout.video_layout, null, false);
        ImageView imageView = videoLayoutView.findViewById(R.id.video_thumbnail);

        try {
            URL url = new URL(imageUrl);       // url 이미지 가져옴
            Glide.with(this).load(url).into(imageView);     // 가져와서 이미지 뷰에 추가
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);     // 이미지 뷰 크기에 맞춰 이미지 확대

        videoLinearLayout.addView(videoLayoutView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            }
        });
    }

}
