package com.example.frontapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GroceryListView extends LinearLayout {
    TextView nameTextView, part;

    public GroceryListView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.no_meet_drop_down, this, true);

        nameTextView.findViewById(R.id.no_meet_name);
    }

    public void setId(String name) {
        nameTextView.setText(name);
    }

}
