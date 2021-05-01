package com.example.frontapp;

public class Ingredient {
    String name;
    String freshLevel;
    boolean isChecked;

    public Ingredient(String name, String freshLevel) {
        this.name = name;
        this.freshLevel = freshLevel;
        this.isChecked = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFreshLevel(String freshLevel) {
        this.freshLevel = freshLevel;
    }

    public String getFreshLevel() {
        return freshLevel;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Boolean isCheck() {
        return this.isChecked;
    }
}
