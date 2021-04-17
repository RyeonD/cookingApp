package com.example.frontapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Preview extends Thread {
    // Log 출력 시 사용
    private final static String TAG = "Preview: ";

    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;    // 카메라 기기
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;


    public Preview(Context context, TextureView textureView) {
        mContext = context;
        mTextureView = textureView;
    }

    private String getBackFacingCameraId(CameraManager cameraManager) {
        try {
            // getCameraIdList : 해당 기기의 모든 카메라 정보를 담은 List
            for (final String cameraId: cameraManager.getCameraIdList()) {
                // getCameraCharacteristics(cameraId) : 해당 cameraId를 가진 카메라 객체(정보)를 반환
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                // characteristics.get(key) : key에 헤당하는 값을 반환
                // get(CameraCharacteristics.LENS_FACING) : 해당 cameraId를 가진 카메라의 카메라 방향을 반환
                // (전면)LENS_FACING_FRONT : 1 / (후면)LENS_FACING_BACK : 2 / (기타)LENS_FACING_EXTERNAL : 3
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                // 카메라가 후면카메라일 경우에만 cameraId 반환
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK)
                    return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera() {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "open Camera E");

        try {
            String cameraId = getBackFacingCameraId(cameraManager);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) : 해당 카메라의 각종 카메라 지원 정보들을 반환
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // getOutputSizes() : 해당 카메라가 지원하는 크기 목록
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
            if(permissionCamera == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, CameraActivity.REQUEST_CAMERA);
            }
            else {
                cameraManager.openCamera(cameraId, mStateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "open Camera X");
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        // TextureView의 SurfaceTexture 사용 준비 완료되면 호출됨
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);
            openCamera();
        }

        // TextureView의 SurfaceTexture의 버퍼 크기 변경  호출됨
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        // TextureView의 SurfaceTexture 업데이트 시 호출됨
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        // TextureView의 SurfaceTexture 종료  호출됨
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
    };

    // 카메라가 켜진다면 카메라 상태 콜백함수 실행
    // 카메라 장치의 상태 업데이트를 위한 콜백 개체
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        // 카메라 켜지면 자동 실행
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }

        // 카메라 장치 더 이상 사용 불가 시
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "onDisconnected");
        }

        // 카메라 장치 에러 시
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "onError");
        }
    };

    // 실질적인 행동 함수
    protected void startPreview() {
        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG, "texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
//        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler mBackgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 카메라 다시 켜
    public void setSurfaceTextureListener() {
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        setSurfaceTextureListener();
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        Log.d(TAG, "onPause");
        try {
            mCameraOpenCloseLock.acquire();
            if(null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d(TAG, "Camera Device Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public static Intent takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File tempFile = createImageFile();
//        Uri uri = Uri.fromFile(tempFile);
        return intent;
    }

    // https://black-jin0427.tistory.com/120
}
