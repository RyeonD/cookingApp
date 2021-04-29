package com.example.frontapp;

public class GroceryList {
    String name;
    String part;

    public GroceryList(){}

    public GroceryList(String name){
        this.name = name;
    }

    public String getName(){ return name; }

    public String getPart(){ return part; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPart(String part) {
        this.part = part;
    }
}
