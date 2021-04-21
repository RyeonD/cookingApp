package com.example.frontapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import static androidx.core.content.ContextCompat.startActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Preview extends Thread {
    // Log 출력 시 사용
    private final static String TAG = "Preview: ";

    private CameraCharacteristics characteristics;
    private StreamConfigurationMap map;
    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;    // 카메라 기기
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;
    private ImageReader mCaptureBuffer;
    private int mWidth;
    private int mHeight;
    private String today;


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
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) : 해당 카메라의 각종 카메라 지원 정보들을 반환
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
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
    public void setSurfaceTextureListener(TextureView textureView) {
        textureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        setSurfaceTextureListener(mTextureView);
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

    public void takePicture(Intent intent) {
        today = todayDate();
        if(null == mCameraDevice) {
            Log.e(TAG,"mCameraDevice is null, return");
            return;
        }

        try{
            Size [] jpegSizes = null;
            if(map != null){
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
            }
            mWidth = 640;
            mHeight = 480;
            if(jpegSizes != null && 0 < jpegSizes.length) {
                mWidth = jpegSizes[0].getWidth();
                mHeight = jpegSizes[0].getHeight();
                Log.e("TAG", "width: "+mWidth+" / height: "+mHeight);
            }

            // ImageReader : surface에 랜더링된 이미지 데이터에 직접 액세스
            ImageReader reader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>();
            // 사용 가능한 surface를 가져옴
            outputSurfaces.add(reader.getSurface());
            // new Surface 생성 - TextureView 안에 있는 surface로
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 출력할 화면
            captureBuilder.addTarget(reader.getSurface());
            // 출력할 화면에 넣을 데이터
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(image != null) {
                            image.close();
                            reader.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        Log.e(TAG, today);
                        // 여기서 값을 가져와야 실행 되는디..
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

                        // 파일 저장
                        output = new FileOutputStream(Environment.getExternalStorageDirectory()+"/Pictures/image_"+today+".jpg");
                        output.write(bytes);
                    } finally {
                        if(null != output) {
                            output.close();
                            Log.e(TAG,"완료");
                        }
                    }
                }
            };
            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    startPreview();
                }

            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String todayDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

        return dateFormat.format(date);
    }
}
