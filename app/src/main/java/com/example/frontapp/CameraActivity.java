package com.example.frontapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {
    String appName = "App Name";
    private final static String TAG = "CameraActivity: ";
    static final int PERMISSIONS_REQUEST_CODE = 1001;
    private String[] PERMISSIONS  = {Manifest.permission.CAMERA};

    static final int REQUEST_CAMERA = 1;
    private TextureView mCameraTextureView;
    private Preview mPreview;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraTextureView = (TextureView) findViewById(R.id.textureView);
        mPreview = new Preview(this, mCameraTextureView);

        mPreview.onResume();

        ImageButton captureBtn = findViewById(R.id.capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), PhotoCheckActivity.class);
                mPreview.takePicture(intent);
                startActivity(intent);
            }
        });
    }

    // 권한 대화창 실행 후 권한이 허가된 경우와 허가되지 않은 경우 동작 재 정의 필요
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
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }
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

        // 권한이 있다면 카메라 실행
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "권한 이미 있음", Toast.LENGTH_LONG).show();
            mPreview.onResume();
        }
    }

    // 권한 부여 전 "앱 실행을 위해 권한 부여가 꼭 필요" 문구 출력 - 실행 코드에 없음. 필요없으면 삭제 해야
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
}
