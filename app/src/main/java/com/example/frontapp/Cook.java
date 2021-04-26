package com.example.frontapp;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Cook implements Parcelable {

    private String name;
    private String img;
    private String ingredients;
    private String recipes;

    public Cook() {

    }

    public Cook(String name, String img, String ingredients, String recipes) {
        this.name = name;
        this.img = img;
        this.ingredients = ingredients;
        this.recipes = recipes;
    }

    public Cook(Parcel in) {
        name = in.readString();
        img = in.readString();
        ingredients = in.readString();
        recipes = in.readString();
    }

    public static final Creator<Cook> CREATOR = new Creator<Cook>() {
        @Override
        public Cook createFromParcel(Parcel in) {
            return new Cook(in);
        }

        @Override
        public Cook[] newArray(int size) {
            return new Cook[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(img);
        dest.writeString(ingredients);
        dest.writeString(recipes);
    }
}
