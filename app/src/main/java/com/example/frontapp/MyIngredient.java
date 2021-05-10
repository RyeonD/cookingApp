package com.example.frontapp;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class MyIngredient {
    private String name;
    private String freshness;
    private boolean checkbox;

    public MyIngredient(String name, String freshness) {
        this.name = name;
        this.freshness = freshness;
        this.checkbox = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFreshness(String freshness) {
        this.freshness = freshness;
    }

    public String getFreshness() {
        return freshness;
    }

    public Drawable getBackground(Context context){
        switch (freshness) {
            case "양호": {
                return ContextCompat.getDrawable(context, R.drawable.ingredient_corner_level1);
            }
            case "위험": {
                return ContextCompat.getDrawable(context, R.drawable.ingredient_corner_level3);
            }
            default: {
                return ContextCompat.getDrawable(context, R.drawable.ingredient_corner_level4);
            }
        }
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public boolean getCheckbox() {
        return checkbox;
    }
}
