package com.example.frontapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroceryListAdapter extends BaseAdapter {
    private static String TAG = "GroceryListAdapter";

    private ArrayList<GroceryList> groceryLists = new ArrayList<GroceryList>();     // 재료 리스트(다른 Activity에서 받아옴)
    private TextView textView;  // 재료 이름 넣을 TextView
    private Spinner spinner;    // 고기의 경우 dropdown 이용하여 선택 시 사용할 Spinner

    // Spinner에 들어가는 값을 체크하기 위한 것("부위 선택"이면 다음 페이지로 넘어가지 않도록 하기 위한 것)
    private HashMap <String, String> checkSpinnerMap = new HashMap<String, String>();

    // 재료 목록 받아와 현재 클래스의 groceryLists 변수 정의
    public GroceryListAdapter(ArrayList<GroceryList> data) {
        groceryLists = data;
    }

    // 현재 클래스의 groceryLists 변수 크기 반환
    @Override
    public int getCount() {
        return groceryLists.size();
    }

    // 현재 클래스의 groceryLists 변수 안 position 위치에 있는 item 반환
    @Override
    public Object getItem(int position) {
        return groceryLists.get(position);
    }

    // 현재 클래스의 groceryLists 변수 안 해당 item이 있는 position 반환???
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 현재 클래스의 groceryLists 변수 안 item들을 view에 출력(재료 리스트 출력)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        GroceryList grocery = groceryLists.get(position);

        if(convertView == null) {
            holder = new ViewHolder();

            // "고기"라는 단어가 포함된 재료(닭고기, 돼지고기, 소고기)의 경우 부위 선택(Spinner 출력)
            if(grocery.getName().contains("고기")) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.meet_drop_down, parent, false);
                textView = (TextView) convertView.findViewById(R.id.meet_row_name);
                spinner = makeTableWithSpinner(grocery.getName(), convertView);
            }
            else {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_meet_drop_down, parent, false);
                textView = (TextView) convertView.findViewById(R.id.no_meet_name);
            }

            textViewStyle(textView);    // text의 크기, 색깔 등 디자인 조정
            holder.textView = textView;

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(grocery.getName());

        return convertView;
    }

    // 고기 부위 선택이 모두 되어 있는지 확인(True면 재료 리스트 반환)
    public String [] getNext() {
        // 부위 선택 확인
        Iterator iterator = checkSpinnerMap.keySet().iterator();
        for(Map.Entry<String, String> entry : checkSpinnerMap.entrySet()) {
            Log.e("확인", entry.getKey()+" / "+entry.getValue().toString());
            if(entry.getValue().contains("부위 선택"))
                return null;
        }

        // True의 경우 실행 - 고기의 경우 부위까지 출력
        String [] groceries = new String[groceryLists.size()];
        iterator = checkSpinnerMap.keySet().iterator();
        String s;
        for(int i = 0; i < groceries.length; i++) {
            groceries[i] = groceryLists.get(i).getName();
            if(groceries[i].contains("고기")) {
                groceries[i] += ("("+checkSpinnerMap.get(groceries[i])+")");
            }
        }

        return groceries;
    }

    static class ViewHolder {
        TextView textView;
    }

    // spinner(드롭다운) 설정
    private Spinner makeTableWithSpinner(String name, View view) {
        Spinner spinner = view.findViewById(R.id.meet_row_spinner);
        ArrayAdapter<CharSequence> adapter = null;
        // 어떤 고기냐에 따라 다른 부위 목록 가져옴
        switch (name){
            case "닭고기":
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.chickenArray, android.R.layout.simple_dropdown_item_1line);
                break;
            case "소고기" :
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.beefArray, android.R.layout.simple_dropdown_item_1line);
                break;
            case "돼지고기":
                adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.porkArray, android.R.layout.simple_dropdown_item_1line);
                break;
            default:
                break;
        }

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);    // spinner의 기본값(가져온 목록의 맨 처음값 = "부위 선택")

        checkSpinnerMap.put(name, "부위 선택");
        checkSpinner(name, spinner);    // spinner를 통해 부위가 선택된 경우 동작 설정

        return spinner;
    }

    // 드롭 다운 선택시 배경 변경 - "부위 선택"(선택하지 않음)=red, "부위 선택" 외(부위 선택됨)=white
    private void checkSpinner(String name, Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                // 부위가 선택되지 않았으면 - spinner 배경 red
                // 부위 선택 상관 없이 현재 어떤게 선택되어있는지 checkSpinnerMap에 삽입
                if(item.contains("부위 선택")) {
                    spinner.setBackgroundColor(Color.rgb(255, 110, 110));
                    checkSpinnerMap.put(name, item);
                }
                else {
                    spinner.setBackgroundColor(Color.argb(0, 255, 255, 255));
                    checkSpinnerMap.put(name, item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // item들 출력 시 text 크기 및 위치, color 설정
    public void textViewStyle(TextView textView) {
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(Dimension.SP, 17);
        textView.setTextColor(Color.BLACK);
    }
}
