package com.example.frontapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private JSONObject jsonObject;
    private LinearLayout groceryTable;
    private int rowId = 0;
    private Map <Integer, String> groceries = new HashMap<Integer, String>();
    ArrayList<Grocery> arrayList = new ArrayList<>();
    GroceryListAdapter adapter;
    private String [] groceryList;
    ListView listView;
    private Intent intent, image_intent;
    private File imgFile;
    private CheckTypesTask task;

    SpeechRecognizer mRecognizer;
    Intent speechIntent;
    AlertDialog speechAlert;
    boolean speechOnOff = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_grocery_list_in_photo);
        image_intent = getIntent();
//        Bitmap bitmap = (Bitmap) image_intent.getParcelableExtra("img");
        String s3_upload_file = image_intent.getStringExtra("img");

        groceryTable = findViewById(R.id.scroll_view_add_layout);

        // json ?????? try-catch
        try {
            jsonObject = getPhotoResult();
        } catch (IOException e) {
            e.printStackTrace();
        }

         // ????????? json ????????? ????????? ?????? ??????
        if(jsonObject != null) {
            outputTable();
        }

//        task = new CheckTypesTask();
//        task.execute();
//
//        imgFile = new File(getCacheDir(), s3_upload_file);
//        uploadWithTransferUtility(s3_upload_file);`

        // ?????? ?????? ?????? ??????
        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_LONG).show();

                for(int i = arrayList.size()-1; i >= 0; i--) {
                    Grocery grocery = arrayList.get(i);
                    if(grocery.isSeletced()) {
                        arrayList.remove(i);
                    }
                }

                adapter = new GroceryListAdapter(arrayList);
                listView.setAdapter(adapter);
            }
        });

        // ?????? ?????? ?????? ??????
        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ????????? ?????????
                groceryAddDialog();
            }
        });

        // ?????? ?????? ??????
        findViewById(R.id.grocery_list_in_photo_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????? ??????
                finish();

                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("camera", true);
                startActivity(intent);
            }
        });

        // NEXT ?????? ??????(????????? ?????? ???????????? ??????)
        findViewById(R.id.grocery_list_in_photo_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groceryList = adapter.getNext();    // ?????? ?????? ??????: ?????? ??????(String Array), ?????? ?????? ?????????: null

                // ?????? ????????? ?????????????????? ??????
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

    private class CheckTypesTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(GroceryListInPhotoActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("??????????????? ????????? ??????????????????");

            // show dialog
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                while(true) {
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    // bitmap to jpg
    public String saveBitmapToJpg(Bitmap bitmap , String name) {

        bitmap = Bitmap.createScaledBitmap(bitmap, 12000, 9000, true);

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
    public void uploadWithTransferUtility(String fileName) {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:47d0f0b0-3957-417f-a59d-334249795498", // ?????? ?????? ??? ID
                Regions.AP_NORTHEAST_2 // ??????
        );
        AmazonS3 amazonS3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(amazonS3).context(this).build();
        TransferNetworkLossHandler.getInstance(this);

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
                            Log.e(TAG, "?????? = " + t.getMessage());
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

    private void parseModelData(String response)
    {
        try
        {
            jsonObject = new JSONObject(response.replace("\"",""));

            if (jsonObject.getString("success").equals("true"))
            {
                if(jsonObject != null) {
                    task.onPostExecute("??????");
                    outputTable();
                }
                imgFile.delete();

            } else {//?????????
                Toast.makeText( getApplicationContext(), "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT ).show();
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // AWS?????? ????????? json ???????????? ????????? ????????? ?????????
    private JSONObject getPhotoResult() throws IOException {
        AssetManager assetManager = getAssets();
        String filename = "jsons/cameraresult.json";

        // ?????? ????????????
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

            // json ?????? ?????? ??? ??????
            return  new JSONObject(buffer.toString());
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ?????? ??? ???????????? ???????????? ??????
    public void outputTable() {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String grocery_name = keys.next().toString();
            String grocery_count = null;
            try {
                grocery_count = jsonObject.get(grocery_name).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (grocery_name.equals("success")) {
                continue;
            }
            arrayList.add(new Grocery(grocery_name, grocery_count));
            Log.e(TAG, grocery_name);
        }

        // Adapter ??????
        adapter = new GroceryListAdapter(arrayList);

        listView = findViewById(R.id.grocery_table_layout);
        listView.setAdapter(adapter);

    }

    // ????????? - ?????? ?????? ????????? ??? ?????????
    public void showDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("??????")
                .setMessage("?????? ????????? ???????????? ???????????????. ????????? ??????????????????.")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "?????? ??????");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    // ?????? ?????? ?????? ?????? ??? ?????? ??? ?????? ???????????? dialog ???
    private void groceryAddDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("?????? ?????? ??????")
                .setMessage("?????? ?????? ????????? ??????????????????.")
                .setNegativeButton("?????? ??????", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groceryTextAddDialog();
                    }
                }).setPositiveButton("?????? ??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    groceryVoiceAddDialog();
                }
            });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void groceryTextAddDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                .setTitle("??????")
                .setView(view)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText textPaint = view.findViewById(R.id.edit_grocery_name);
                        arrayList.add(new Grocery(textPaint.getText().toString(), "1"));
                        adapter = new GroceryListAdapter(arrayList);
                        listView.setAdapter(adapter);
                    }
                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "???????????????");
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void groceryVoiceAddDialog() {
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            //????????? ???????????? ?????? ??????
        } else {
            //????????? ????????? ??????
            try {
                mRecognizer.startListening(speechIntent);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }

    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            System.out.println("onReadyForSpeech.........................");
            if(!speechOnOff) {
                speechOnOff = true;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                        .setTitle("?????? ??????")
                        .setMessage("????????? ????????? ??????????????????.");

                speechAlert = alertBuilder.create();
                speechAlert.show();
            }
        }
        @Override
        public void onBeginningOfSpeech() {
            System.out.println("onBeginningOfSpeech.........................");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            System.out.println("onRmsChanged.........................");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            System.out.println("onBufferReceived.........................");
        }

        @Override
        public void onEndOfSpeech() {
            System.out.println("onEndOfSpeech.........................");
        }

        @Override
        public void onError(int error) {
            speechAlert.dismiss();

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                    .setTitle("?????? ??????")
                    .setMessage("????????? ?????? ??????????????????.");

            speechAlert = alertBuilder.create();
            speechAlert.show();

            mRecognizer.startListening(speechIntent);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            System.out.println("onPartialResults.........................");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            System.out.println("onEvent.........................");
        }

        @Override
        public void onResults(Bundle results) {
            speechAlert.dismiss();

            String key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Log.e(TAG, rs[0]);

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroceryListInPhotoActivity.this)
                    .setTitle(rs[0] + " ???(???) ?????????????????????????")
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            arrayList.add(new Grocery(rs[0], "1"));
                            adapter = new GroceryListAdapter(arrayList);
                            listView.setAdapter(adapter);
                        }
                    }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e(TAG, "???????????????");
                        }
                    });

            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();

            speechOnOff = false;
//            mRecognizer.startListening(speechIntent); //??????????????? ?????? ?????? ???????????? ????????? ?????? ????????? ??????
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
            speechOnOff = false;
        }
    }
}
