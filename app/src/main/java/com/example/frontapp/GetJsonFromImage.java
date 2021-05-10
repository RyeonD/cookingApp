package com.example.frontapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetJsonFromImage {
    private String TAG = "getJsonFromImage";
    private JSONObject jsonObject;
    private File imgFile;
    private boolean completeGetJson;
    private Context mContext;

    public GetJsonFromImage(Context mContext) {
        this.mContext = mContext;
    }

    // bitmap to jpg
    public String saveBitmapToJpg(Bitmap bitmap , String name) {

        File storage = mContext.getCacheDir();  //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
        String fileName = name + ".jpg";
        imgFile = new File(storage, fileName);
        try {
            imgFile.createNewFile();
            FileOutputStream out = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            Log.e("saveBitmapToJpg","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("saveBitmapToJpg","IOException : " + e.getMessage());
        }
        Log.d("imgPath" , mContext.getCacheDir() + "/" + fileName);
        return fileName;
    }

    // s3 upload
    public void uploadWithTransferUtility(String fileName) {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                mContext,
                "ap-northeast-2:47d0f0b0-3957-417f-a59d-334249795498", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        AmazonS3 amazonS3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(amazonS3).context(mContext).build();
        TransferNetworkLossHandler.getInstance(mContext);

        TransferObserver uploadObserver = transferUtility.upload("sagemaker-deploy-test", fileName, imgFile);
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());

                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(TAG, "Upload is completed. ");

                    RetrofitClass retrofitClass = new RetrofitClass("https://46l1iikk90.execute-api.ap-northeast-2.amazonaws.com/");
                    GroceryListInPhotoInterface api = retrofitClass.retrofit.create(GroceryListInPhotoInterface.class);
                    Map<String, String> map = new HashMap<>();
                    map.put("bucket", "sagemaker-deploy-test");
                    map.put("image_url", fileName);
                    JSONObject json = new JSONObject(map);
//                    Call<String> call = api.getModelResult("sagemaker-deploy-test", fileName);
                    Call<String> call = api.getModelResult(json);
                    call.enqueue(new Callback<String>()
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                        {
                            if (response.isSuccessful() && response.body() != null)
                            {
                                Log.e("onSuccess", response.body());

                                String jsonResponse = response.body();
                                parseModelData(jsonResponse);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                        {
                            Log.e(TAG, "에러 = " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;
                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        });
    }

    public void parseModelData(String response)
    {
        try
        {
            jsonObject = new JSONObject(response);
            if (jsonObject.getString("success").equals("true"))
            {
                if(jsonObject != null) {
                    completeGetJson = true;
                }
                imgFile.delete();

            } else {//실패시
                Toast.makeText( mContext, "이미지 분석에 실패했습니다.", Toast.LENGTH_SHORT ).show();
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getJsonObject() {
        if(completeGetJson)
            return jsonObject;
        else
            return null;
    }
}
