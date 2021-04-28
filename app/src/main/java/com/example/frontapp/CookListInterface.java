package com.example.frontapp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CookListInterface {
    String REGIST_URL = "http://a7c6ab7c81d8.ngrok.io/";

    @FormUrlEncoded
    @POST("test/recipe/")
    Call<String> getRecipe(
            @Field("recipe_list") String IngredientList
    );
}
