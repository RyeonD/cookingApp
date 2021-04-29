package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CookListInterface {
    @FormUrlEncoded
    @POST("test/recipe/")
    Call<String> getRecipe(
            @Field("grocery_list") String groceryList,
            @Field("main_list") String mainList
    );
}
