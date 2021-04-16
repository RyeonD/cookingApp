package com.example.frontapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraActivity extends AppCompatActivity {
    String appName = "App Name";
    static final int PERMISSIONS_REQUEST_CODE = 1001;
    String[] PERMISSIONS  = {Manifest.permission.CAMERA};
    TextureView textureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        permissionCheck();

        Button nextBtn = findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"다음으로...", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 권한 확인 및 권한 부여
    private void permissionCheck() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        // Pakage.PERMISSION_GRANTED -> 권한 있음
        // Pakage.PERMISSION_DENIED -> 권한 없음
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionDialog(this);
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

        // 권한이 있다면 카메라 실행
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "권한 이미 있음", Toast.LENGTH_LONG).show();
//            startCamera();
        }
    }

    // 권한 부여 전 "앱 실행을 위해 권한 부여가 꼭 필요" 문구 출력
    private void permissionDialog(Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("요리를 추천 받으려면 카메라가 필요합니다. "+appName+"이(가) 카메라를 사용할 수 있도록 허용하시겠습니까?")
//        .setIcon(R.mipmap.ic_launcher)
        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "카메라 권한 승인", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }).show();
    }

    // 카메라 실행
    // https://salix97.tistory.com/60
    // https://wiserloner.tistory.com/1307
    public void startCamera() {
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }
}
