package com.example.frontapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroceryListInPhotoActivity extends AppCompatActivity {
    private static String TAG = "GroceryListInPhotoActivity: ";
    private JSONObject jsonObject;
    private LinearLayout groceryTable;
    private int rowId = 0;
    private Map <Integer, String> groceries = new HashMap<Integer, String>();
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
    // 재료 List 확인
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list_in_photo);
        image_intent = getIntent();
        Bitmap bitmap = (Bitmap) image_intent.getParcelableExtra("img");

        groceryTable = findViewById(R.id.scroll_view_add_layout);

        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date timeInDate = new Date(time);
        String fileName = sdf.format(timeInDate);
        String s3_upload_file = saveBitmapToJpg(bitmap, fileName);

        uploadWithTransferUtility(s3_upload_file);
        Log.e(TAG, "s3 upload finish");
        File storage = getCacheDir();  //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
        File file = new File(storage, fileName);
        file.delete();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e(TAG, "요청 응답");
                    jsonObject = new JSONObject( response );
                    boolean success = jsonObject.getBoolean( "success" );
                    Log.e(TAG, "요청 응답 완료");
                    if(success) {//로그인 성공시
                        // json 파일 try-catch
//                        try {
//                            jsonObject = getPhotoResult();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        // 가져온 json 파일이 있다면 목록 출력
                        if(jsonObject != null) {
                            outputTable();
                        }

                    } else {//실패시
                        Toast.makeText( getApplicationContext(), "이미지 분석에 실패했습니다.", Toast.LENGTH_SHORT ).show();
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        GroceryPhotoModelRequest groceryphotomodelrequest = new GroceryPhotoModelRequest(s3_upload_file, responseListener );
        RequestQueue queue = Volley.newRequestQueue( GroceryListInPhotoActivity.this );
        queue.add( groceryphotomodelrequest );
        Log.e(TAG, "queue add");

        // 목록 수정 버튼 클릭
        findViewById(R.id.grocery_list_in_photo_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "목록 수정 버튼");
                Toast.makeText(getApplicationContext(), "목록 수정 버튼 눌림", Toast.LENGTH_LONG).show();
            }
        });

        // NEXT 추천 레시피 목록 출력
        findViewById(R.id.grocery_list_in_photo_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "요리 검색 버튼 눌림", Toast.LENGTH_LONG).show();

                boolean nextPageLoad = true;
                String [] groceryList = new String[groceries.size()];
                for(Integer i : groceries.keySet()) {
                    if(groceries.get(i).contains("부위")){
                        nextPageLoad = false;
                        break;
                    }
                    else {
                        groceryList[i] = groceries.get(i);
                    }
                }

                if(nextPageLoad) {
                    intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                    intent.putExtra("groceryList", groceryList);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "부위 선택 필요", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // AWS에서 가져온 json 파일에서 필요한 데이터 빼오기
    private JSONObject getPhotoResult() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/카메라인식결과.json";

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
            int grocery_count = 0;
            try {
                grocery_count = jsonObject.getInt(grocery_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 목록 테이블에 삽입 - 고기의 경우 스피너(드롭다운) 출력
            if(grocery_name.contains("고기")) {
                String meet = new String();
                switch (grocery_name) {
                    case "닭고기": meet = "닭고기"; break;
                    case "소고기": meet = "소고기"; break;
                    default: meet = "돼지고기"; break;
                }

                for(int i = 0; i < grocery_count; i++)
                    makeTableWithSpinner(meet, grocery_name, Integer.toString(grocery_count));
            }
            else {
                makeTable(grocery_name, Integer.toString(grocery_count));
                groceries.put(rowId++, grocery_name);
            }
        }
    }

    public void textViewStyle(TextView textView) {
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }

    // 가져온 데이터 출력
    public void makeTable(String name, String cnt) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.no_meet_drop_down, null);

        TextView nameTextView = view.findViewById(R.id.no_meet_name);
        nameTextView.setText(name);
        textViewStyle(nameTextView);

        TextView noMeetTextView = view.findViewById(R.id.no_meet);
        textViewStyle(noMeetTextView);

        TextView countTextView = view.findViewById(R.id.no_meet_count);
        countTextView.setText(cnt);
        textViewStyle(countTextView);

        groceryTable.addView(view);
    }

    // spinner와 데이터 출력
    public void makeTableWithSpinner(String meetArray, String name, String count) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.meet_drop_down, null);

        TextView nameTextView = view.findViewById(R.id.meet_row_name);
        nameTextView.setText(name);
        textViewStyle(nameTextView);

        Spinner spinner = view.findViewById(R.id.meet_row_spinner);
        ArrayAdapter<CharSequence> adapter;
        switch (meetArray){
            case "닭고기":
                adapter = ArrayAdapter.createFromResource(this, R.array.chickenArray, android.R.layout.simple_dropdown_item_1line);
                break;
            case "소고기" :
                adapter = ArrayAdapter.createFromResource(this, R.array.beefArray, android.R.layout.simple_dropdown_item_1line);
                break;
            default:
                adapter = ArrayAdapter.createFromResource(this, R.array.porkArray, android.R.layout.simple_dropdown_item_1line);
                break;
        }

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        checkSpinner(rowId++, name, spinner);

        groceryTable.addView(view);
    }

    public void checkSpinner(int spinnerId, String name, Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                groceries.put(spinnerId, name+" "+item);
                if(item.contains("부위 선택"))
                    spinner.setBackgroundColor(Color.rgb(255, 110, 110));
                else
                    spinner.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
