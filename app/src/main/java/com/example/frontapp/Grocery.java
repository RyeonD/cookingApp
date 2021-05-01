package com.example.frontapp;

import android.util.Log;

public class Grocery {
    String name;
    String part;
    boolean seletced;
    boolean meet;

    public Grocery(){}

    public Grocery(String name){
        this.name = name;
        this.seletced = false;
    }

    public String getName(){ return name; }

    public String getPart(){ return part; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public boolean isSeletced() {
        return seletced;
    }

    public void setSeletced(boolean seletced) {
        this.seletced = seletced;
    }

    public boolean isMeet() {
        if(!name.contains("고기")) {
            Log.e("확인", "고기 XXXXXXXXX");
            meet = false;
        }
        else {
            Log.e("확인", "고기 OOOOOOOOOOOO");
            meet = true;
        }

        return meet;
    }
}
