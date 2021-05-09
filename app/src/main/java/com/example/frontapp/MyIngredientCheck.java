package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MyIngredientCheck {

    @FormUrlEncoded
    @POST("ingredient_user_chk")
    Call<String> getCheckIngredient(
            @Field("ingredient") String ingredient
    );
}
