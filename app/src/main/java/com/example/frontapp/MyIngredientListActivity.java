package com.example.frontapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONArray;
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

public class MyIngredientListActivity extends AppCompatActivity {
    private static String TAG = "MyIngredientListActivity";

    private static final String PREF_USER_ID = "MyAutoLogin";
    SharedPreferences sharedPreferencesUser;

    final static int TAKE_PICTURE = 1;
    final static int REQUEST_TAKE_PHOTO = 1;
    private File imgFile;
    private JSONObject jsonObject;

    private static final String PREF_USER_INGREDIENT = "MyIngredientList";
    SharedPreferences sharedPreferencesUserIngredient;
    SharedPreferences.Editor editor;

    private GridLayout ingredientGrid;

    private int fresh_first = 0;
    private int fresh_second = 0;
    private int fresh_third = 0;

    private JSONArray jsonArray;
    private ArrayList<MyIngredient> ingredientList;
    private ArrayList<String> deleteIngredient;
    private ArrayList<String> addIngredient;

    private ArrayAdapter<CharSequence> adapter;
    private Spinner spinner;

    private CheckTypesTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteIngredient = new ArrayList<>();
        addIngredient = new ArrayList<>();

        sharedPreferencesUser = getSharedPreferences(PREF_USER_ID, MODE_PRIVATE);
        sharedPreferencesUserIngredient = getSharedPreferences(PREF_USER_INGREDIENT, MODE_PRIVATE);
        editor = sharedPreferencesUserIngredient.edit();

        task = new CheckTypesTask();

        setOriginalPage();
    }

    // Orinal Page ?????? ??? ??????
    private void setOriginalPage() {
        setContentView(R.layout.activity_my_ingredient_list);

        // ?????? ??????
        TextView textView = findViewById(R.id.user_name_text);
        textView.setText(getUserName());

        adapter = ArrayAdapter.createFromResource(this, R.array.orderArray, android.R.layout.simple_spinner_dropdown_item);
        spinner = findViewById(R.id.order_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        ingredientGrid = findViewById(R.id.grid_layout);
        getChangeIngredientList();

//        if(ingredientList != null){
//            getChangeIngredientList();
//        }
//        else
//            getIngredientList();

        findViewById(R.id.ingredient_page_edit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChangePage();
            }
        });

        findViewById(R.id.my_ingredient_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainGrocerySelectionActivity.class);
                String [] groceryList = new String[ingredientList.size()];
                for(int i = 0; i < groceryList.length; i++) {
                    groceryList[i] = ingredientList.get(i).getName();
                }
                intent.putExtra("update", false);
                intent.putExtra("groceryList", groceryList);
                startActivity(intent);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                if(item.contains("?????????")) {
                    outputOriginalPage();
                }
                else {
                    ArrayList <MyIngredient> levelList = new ArrayList<>();
                    int start = 0;
                    int end = 0;
                    for(MyIngredient myIngredient:ingredientList) {
                        switch (myIngredient.getFreshness()) {
                            case "??????": {
                                levelList.add(start++, myIngredient);
                                end++;
                                break;
                            }
                            case "??????": {
                                levelList.add(end++, myIngredient);
                                break;
                            }
                            default: {
                                levelList.add(myIngredient);
                                break;
                            }
                        }
                    }

                    ingredientGrid.removeAllViews();
                    for(MyIngredient myIngredient:levelList) {
                        setIngredientListInOrigin(myIngredient);
                    }


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // ?????? Page ?????? ??? ??????
    private void setChangePage() {
        setContentView(R.layout.activity_my_ingredient_list_resive);

        outputChangePage();

        deleteIngredient = new ArrayList<>();
        addIngredient = new ArrayList<>();

        // ?????? ??????
        findViewById(R.id.ingredient_page_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientAddDialog();
            }
        });

        // ?????? ??????
        findViewById(R.id.ingredient_page_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "?????? ??? ??????");
                for(int i = ingredientList.size()-1; i >= 0; i--) {
                    if(ingredientList.get(i).getCheckbox()) {
                        deleteIngredient.add(ingredientList.get(i).getName());
                        ingredientList.remove(i);
                    }
                }
                outputChangePage();
            }
        });

        // ?????? ??????
        findViewById(R.id.ingredient_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOriginalPage();
                Log.e(TAG, addIngredient+"?");
                Log.e(TAG, deleteIngredient+"?");
            }
        });
    }

    // User??? ?????? ?????? ?????? ???????????? - ???????????? ???????????? ?????? ?????? ????????????
    private void getIngredientList1() {
        ingredientList = new ArrayList<>();     // ?????? ??? ?????? ?????? ????????? ????????? ?????? ????????? ???????????? ?????? ??????

        // json ?????? try-catch
        AssetManager assetManager = getAssets();
        String filename = "jsons/loginResult.json";

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

            jsonArray = new JSONArray(buffer.toString());
            Log.e(TAG, jsonArray.toString());

            JSONObject jsonObject = null;
            String name = null;
            String freshness = null;
            for(int i=0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                name = jsonObject.get("sortkey").toString();
                freshness = jsonObject.get("freshness").toString();
                ingredientList.add(new MyIngredient(name, freshness));
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // original ????????? ????????? ??????
    private void outputOriginalPage() {
        ingredientGrid.removeAllViews();

        fresh_first = 0;
        fresh_second = 0;
        fresh_third = 0;

        for(MyIngredient ingredient:ingredientList){
            setIngredientListInOrigin(ingredient);
            countFresh(ingredient.getFreshness());
        }

        editor.putInt("freshLevel1", fresh_first);
        editor.putInt("freshLevel2", fresh_second);
        editor.putInt("freshLevel3", fresh_third);
        editor.commit();

        setFreshCount();
    }

    // ?????? ????????? ????????? ??????
    private void outputChangePage() {
        ingredientGrid = findViewById(R.id.grid_layout);
        ingredientGrid.removeAllViews();

        for(MyIngredient ingredient:ingredientList){
            setIngredientListInChange(ingredient);
        }
    }

    // ?????? ?????? ?????? - Origin Page
    private void setIngredientListInOrigin(MyIngredient ingredient) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientView = inflater.inflate(R.layout.ingredient_object, null, false);
        TextView textView = ingredientView.findViewById(R.id.ingredient_object_name);

        textView.setText(ingredient.getName());
        textView.setBackground(ingredient.getBackground(this));

        ingredientGrid.addView(ingredientView);
    }

    // ?????? ?????? ?????? - ?????? ?????????
    private void setIngredientListInChange(MyIngredient ingredient) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ingredientView = inflater.inflate(R.layout.ingredient_object_check, null, false);
        ConstraintLayout constraintLayout = ingredientView.findViewById(R.id.ingredient_box);
        TextView textView = ingredientView.findViewById(R.id.ingredient_object_name);
        CheckBox checkBox = ingredientView.findViewById(R.id.ingredient_check);

        textView.setText(ingredient.getName());
        Log.e(TAG, ingredient.getName());
        constraintLayout.setBackground(ingredient.getBackground(this));

        ingredientGrid.addView(ingredientView);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    ingredient.setCheckbox(true);
                }
                else {
                    ingredient.setCheckbox(false);
                }
                Log.e(TAG, String.valueOf(ingredient.getCheckbox()));
            }
        });
    }

    // ?????? ????????? ??????(????????? ??????)
    private void countFresh(String freshness){
        switch (freshness) {
            case "??????": fresh_first++; break;
            case "??????": fresh_second++; break;
            default: fresh_third++; break;
        }
    }

    // fresh ?????? ??????
    private void setFreshCount() {
        TextView level1 = findViewById(R.id.level1_cnt);
        TextView level2 = findViewById(R.id.level2_cnt);
        TextView level3 = findViewById(R.id.level3_cnt);

        level1.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("freshLevel1",0)));
        level2.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("freshLevel2",0)));
        level3.setText(Integer.toString(sharedPreferencesUserIngredient.getInt("freshLevel3",0)));
    }

    // ?????? ?????? ?????? dialog ???
    private void ingredientAddDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyIngredientListActivity.this)
                .setTitle("?????? ?????? ??????")
                .setMessage("????????? ?????? -> ?????? ??????\n????????? ???????????? ?????? ?????? -> ?????? ??????")
                .setPositiveButton("?????? ??????", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addDialog();
                    }
                }).setNegativeButton("?????? ??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "?????? ??????");
                        startCamera();
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    // ?????? ?????? - ?????? ?????? ???
    private void addDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grocery_list_add_dialog, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyIngredientListActivity.this)
                .setTitle("?????? ??????")
                .setView(view)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText textPaint = view.findViewById(R.id.edit_grocery_name);
                        addIngredient.add(textPaint.getText().toString());
                        ingredientList.add(new MyIngredient(textPaint.getText().toString(),"??????"));
                        outputChangePage();
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

    // UserID ????????????
    public String getUserId() {
        return sharedPreferencesUser.getString("UserId", "");
    }

    // User??? Name ????????????
    public String getUserName() {
        return sharedPreferencesUser.getString("UserName", "");
    }

    // DB?????? ?????? ?????? ???????????? & ?????? ??????
    private void getChangeIngredientList() {
        ingredientList = new ArrayList<>();
        // ????????? ????????????
        RetrofitClass retrofitClass = new RetrofitClass(5000);
//        RetrofitClass retrofitClass = new RetrofitClass("http://f645f2ae0f52.ngrok.io/");
        MyIngredientList api = retrofitClass.retrofit.create(MyIngredientList.class);
        Log.e(TAG, "?????? ?????? ?????????"+getUserId());

        Call<String> call = api.getUserIngredientList(getUserId(), getAddList(), getDeleteList());
        call.enqueue(new Callback<String>()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e("onSuccess", response.body());

                    String jsonResponse = response.body();
                    try {
                        JSONObject jsonObject = new JSONObject( jsonResponse );
                        Log.e(TAG, jsonObject.toString());
                        if (jsonObject.getString("success").equals("true")) {
                            jsonArray = jsonObject.getJSONArray("result");

                            // ????????? ?????????(?????? ?????? ?????? ??????) ?????? ??????
                            Log.e(TAG, jsonArray.toString());

                            // ????????? ????????? ??????
                            String name = null;
                            String freshness = null;
                            for(int i=0; i < jsonArray.length(); i++) {
                                jsonObject = jsonArray.getJSONObject(i);
                                name = jsonObject.get("sortkey").toString();
                                freshness = jsonObject.get("freshness").toString();
                                ingredientList.add(new MyIngredient(name, freshness));
                            }
                            outputOriginalPage();
                        } else {
                            Toast.makeText( getApplicationContext(), "????????? ??????????????? ??????????????????.", Toast.LENGTH_SHORT ).show();
                            return;
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "?????? ??????");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "?????? = " + t.getMessage());
            }
        });
    }

    private String getAddList() {
        if(addIngredient.size() == 0)
            return "";
        else {
            String result = "";

            for(int i=0; i < addIngredient.size(); i++) {
                result += addIngredient.get(i);

                if(i+1 != addIngredient.size()) {
                    result += " ";
                }
            }
            return result;
        }
    }

    private String getDeleteList() {
        if(deleteIngredient.size() == 0)
            return "";
        else {
            String result = "";

            for(int i=0; i < deleteIngredient.size(); i++) {
                result += deleteIngredient.get(i);

                if(i+1 != deleteIngredient.size()) {
                    result += " ";
                }
            }
            return result;
        }
    }

    // ????????? ??????
    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, TAKE_PICTURE);
        }
    }

    // ????????? ?????? ????????? ????????? ??? ?????????(????????????)??? ??????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO) {
            task.execute();

            Bundle imageBundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) imageBundle.get("data");

            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date timeInDate = new Date(time);
            String fileName = sdf.format(timeInDate);
            String s3_upload_file = saveBitmapToJpg(imageBitmap, fileName);

            uploadWithTransferUtility(s3_upload_file);
        }
    }

    // bitmap to jpg
    public String saveBitmapToJpg(Bitmap bitmap , String name) {

        File storage = this.getCacheDir();  //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
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
        Log.d("imgPath" , this.getCacheDir() + "/" + fileName);
        return fileName;
    }

    // s3 upload
    public void uploadWithTransferUtility(String fileName) {

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                this,
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

    public void parseModelData(String response)
    {
        try
        {
            jsonObject = new JSONObject(response);
            if (jsonObject.getString("success").equals("true"))
            {
                if(jsonObject != null) {
                    task.onPostExecute("??????");
                    ingredientListInPhoto();
                }
                imgFile.delete();

            } else {//?????????
                Toast.makeText( this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT ).show();
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void ingredientListInPhoto() {
        Iterator iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String name = iterator.next().toString();
            if(!name.contains("success")) {
                addIngredient.add(name);
                ingredientList.add(new MyIngredient(name, "??????"));
            }
        }

        // ????????? ????????? ????????? ?????????
        outputChangePage();
    }

    private class CheckTypesTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(MyIngredientListActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("??????????????? ????????? ??????????????????.");

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
}
