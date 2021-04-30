package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.regions.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.frontapp.R.layout.activity_grocery_list_in_photo;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    final static int REQUEST_TAKE_PHOTO = 1;
    private JSONObject jsonObject;
    private LinearLayout groceryTable;
    private int rowId = 0;
    private Map <Integer, String> groceries = new HashMap<Integer, String>();
    ArrayList<GroceryList> arrayList = new ArrayList<>();
    GroceryListAdapter adapter;
    private String [] groceryList;
    ListView listView;
    private Intent intent, image_intent;
    private File imgFile;

    // bitmap to jpg
    public String saveBitmapToJpg(Bitmap bitmap , String name) {
        /**
         * 캐시 디렉토리에 비트맵을 이미지파일로 저장하는 코드입니다.
         *
         * @version target API 28 ★ API29이상은 테스트 하지않았습니다.★
         * @param Bitmap bitmap - 저장하고자 하는 이미지의 비트맵
         * @param String fileName - 저장하고자 하는 이미지의 비트맵
         *
         * File storage = 저장이 될 저장소 위치
         *
         * return = 저장된 이미지의 경로
         *
         * 비트맵에 사용될 스토리지와 이름을 지정하고 이미지파일을 생성합니다.
         * FileOutputStream으로 이미지파일에 비트맵을 추가해줍니다.
         */
        File storage = getCacheDir();  //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
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
        Log.d("imgPath" , getCacheDir() + "/" + fileName);
        return fileName;
    }
    // s3 upload
    private void uploadWithTransferUtility(String fileName) {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:47d0f0b0-3957-417f-a59d-334249795498", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        AmazonS3 amazonS3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(amazonS3).context(this).build();
        TransferNetworkLossHandler.getInstance(this);

        TransferObserver uploadObserver = transferUtility.upload("sagemaker-deploy-test", fileName, imgFile);
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());

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

    private void parseModelData(String response)
    {
        try
        {
            jsonObject = new JSONObject(response);
            if (jsonObject.getString("success").equals("true"))
            {
                if(jsonObject != null) {
                    outputTable();
                }
                imgFile.delete();

            } else {//실패시
                Toast.makeText( getApplicationContext(), "이미지 분석에 실패했습니다.", Toast.LENGTH_SHORT ).show();
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // AWS에서 가져온 json 파일에서 필요한 데이터 빼오기
    private JSONObject getPhotoResult() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/cameraResult.json";

        // 파일 가져오기
        try {
            InputStream data = assetManager.open(filename);
            InputStreamReader dataReader = new InputStreamReader(data);
            BufferedReader reader = new BufferedReader(dataReader);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }

            // json 객체 생성 및 파싱
            return  new JSONObject(buffer.toString());
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 사진 속 식재료를 테이블로 출력
    public void outputTable() {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String grocery_name = keys.next().toString();

            if (grocery_name.equals("success")) {
                continue;
            }
            arrayList.add(new GroceryList(grocery_name));
        }

        // Adapter 생성
        adapter = new GroceryListAdapter(arrayList);

        listView = findViewById(R.id.grocery_table_layout);
        listView.setAdapter(adapter);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                GroceryList groceryList = (GroceryList) parent.getItemAtPosition(position);
//            }
//        });
    }

    // 알림창 - 부위 선택 미완료 시 띄워줌
    public void showDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("알림")
                .setMessage("고기 부위가 선택되지 않았습니다. 부위를 선택해주세요.")
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "다시 선택");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    // 재료 List 확인
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_grocery_list_in_photo);
        image_intent = getIntent();
        Bitmap bitmap = (Bitmap) image_intent.getParcelableExtra("img");

        groceryTable = findViewById(R.id.scroll_view_add_layout);

        // json 파일 try-catch
//        try {
//            jsonObject = getPhotoResult();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//         // 가져온 json 파일이 있다면 목록 출력
//        if(jsonObject != null) {
//            outputTable();
//        }

        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date timeInDate = new Date(time);
        String fileName = sdf.format(timeInDate);
        String s3_upload_file = saveBitmapToJpg(bitmap, fileName);

        uploadWithTransferUtility(s3_upload_file);

        RetrofitClass retrofitClass = new RetrofitClass();
        GroceryListInPhotoInterface api = retrofitClass.retrofit.create(GroceryListInPhotoInterface.class);
        Call<String> call = api.getModelResult("sagemaker-deploy-test", s3_upload_file);
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

        // 재료 항목 삭제 버튼
        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "삭제", Toast.LENGTH_LONG).show();

                for(int i = arrayList.size()-1; i >= 0; i--) {
                    GroceryList grocery = arrayList.get(i);
                    if(grocery.isSeletced()) {
                        arrayList.remove(i);
                    }
                }

                adapter = new GroceryListAdapter(arrayList);
                listView.setAdapter(adapter);
            }
        });

        // 재료 항목 추가 버튼
        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력창 띄우기
                groceryAddDialog();
            }
        });

        // 다시 사진 찍기
        findViewById(R.id.grocery_list_in_photo_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로 가기
                finish();

                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("camera", true);
                startActivity(intent);
            }
        });

        // NEXT 버튼 클릭(주재료 선택 페이지로 이동)
        findViewById(R.id.grocery_list_in_photo_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groceryList = adapter.getNext();    // 부위 선택 완료: 재료 목록(String Array), 부위 선택 미완료: null

                // 부위 선택이 완료되었는지 확인
                if(groceryList != null) {
                    intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                    intent.putExtra("groceryList", groceryList);
                    startActivity(intent);
                }
                else {
                    showDialog();
                }
            }
        });
    }

    private void groceryAddDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("알림")
                .setView(view)
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText textPaint = view.findViewById(R.id.edit_grocery_name);
                        arrayList.add(new GroceryList(textPaint.getText().toString()));
                        adapter = new GroceryListAdapter(arrayList);
                        listView.setAdapter(adapter);
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "취소할꺼임");
                }
            });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

}
