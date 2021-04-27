package com.example.frontapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainGrocerySelectionActivity extends AppCompatActivity {
    private static String TAG = "MainGrocerySelectionActivity: ";
    private Intent intent;
    private String [] groceryList;
    private LinearLayout scrollLayout;
    private int id = 1;

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
                String mainList = new String();

                for(int i = 1; i <= listCnt; i++) {
                    CheckBox checkBox = findViewById(i);
                    if(checkBox.isChecked()){
                        mainList += (checkBox.getText().toString() + " ");
                        cnt++;
                    }
                }

                if(cnt > 3) {
                    showDialog("3개까지만 선택 가능합니다. 다시 선택해주세요.", null);
                }
                else if(cnt > 0 && cnt <= 3) {
                    showDialog("선택된 주재료로 요리를 검색합니다.\n", mainList.substring(0, mainList.length()-1));
                }
                else {
                    showDialog("선택된 주재료가 없습니다. 다시 선택해주세요.", null);
                }
            }
        });
    }

    // 리스트 출력
    public void groceryListOutput(String s) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main_grocery_selection, null);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setText(s);
        checkBox.setId(id++);
        scrollLayout.addView(view);
    }

    // 알림창
    public void showDialog(String s, String mainList) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainGrocerySelectionActivity.this)
                .setTitle("알림");

        if(mainList != null) {
            alertBuilder.setMessage(s+"\""+mainList+"\"")
            .setPositiveButton("레시피 검색", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), CookListActivity.class);
                    intent.putExtra("mainList", mainList);
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
