package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainGrocerySelectionActivity extends AppCompatActivity {
    private static String TAG = "MainGrocerySelectionActivity: ";
    private Intent intent;
    private String [] groceryList;
    private LinearLayout scrollLayout;
    private int id = 1;
    String mainList;

    // 확인한 재료 목록에서 주재료 선택
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_grocery_selection);

        intent = getIntent();
        groceryList = intent.getStringArrayExtra("groceryList");
        scrollLayout = findViewById(R.id.scroll_view_add_layout);

        for(String s: groceryList) {
            groceryListOutput(s);
        }

        // 뒤로 가기
        findViewById(R.id.grocery_selection_prev_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 검색(NEXT)
        findViewById(R.id.grocery_selection_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cnt = 0;
                int listCnt = scrollLayout.getChildCount();
                mainList = new String(); // 선택 된 checkBox의 목록

                // 선택 된 checkBox의 목록 생성(하나의 String으로 생성)
                for(int i = 1; i <= listCnt; i++) {
                    CheckBox checkBox = findViewById(i);
                    if(checkBox.isChecked()){
                        mainList += (checkBox.getText().toString() + " ");
                        cnt++;
                    }
                }

                // 주재료 선택 개수에 따른 알림창 띄움(0개: 선택 필요 / 1~3개: 선택 완료. 검색할지 물어보기 / 4개 이상: 3개까지만 선택 가능. 다시 선택 필요)
                if(cnt > 3) {
                    showDialog("3개까지만 선택 가능합니다. 다시 선택해주세요.", null);
                }
                else if(cnt > 0 && cnt <= 3) {
                    showDialog("선택된 주재료로 요리를 검색합니다.\n", mainList.substring(0, mainList.length()-1));
                }
                else {
                    showDialog("선택된 주재료가 없습니다. 다시 선택해주세요.", null);
                }

                // 서버로 보낼 String 확인
                Log.e(TAG, mainList.replaceAll("[\\(|\\)]",""));
            }
        });
    }

    // 뒤로가기로 페이지에 돌아왔을 때
    @Override
    protected void onStart() {
        super.onStart();
        mainList = null;
    }

    // 재료 리스트 출력
    public void groceryListOutput(String s) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main_grocery_selection, null);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setText(s);
        checkBox.setId(id++);
        scrollLayout.addView(view);
    }

    // 알림창 - 주재료 선택과 미선택 시 각각 다른 알림참 띄움
    public void showDialog(String s, String mainList) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainGrocerySelectionActivity.this)
                .setTitle("알림");

        if(mainList != null) {
            // 주재료 1개 이상 3개 이하일때(주재료 선택 개수는 현재 클래스를 불러온 if-else 문에서 이미 확인 완료)
            alertBuilder.setMessage(s+"\""+mainList+"\"")
                    .setPositiveButton("레시피 검색", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String grocery_list = new String().join(" ", groceryList);
                            String[] ingredientList = new String[2];
                            ingredientList[0] = grocery_list;
                            ingredientList[1] = mainList;

                            Intent intent = new Intent(getApplicationContext(), CookListActivity.class);
                            intent.putExtra("ingredientList", ingredientList);
                            startActivity(intent);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "취소할꺼임");
                }
            });
        }
        else {
            // 주재료 0개 이하 4개 이상일때
            alertBuilder.setMessage(s)
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "다시 선택");
                    }
                });
        }

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
