package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.PUT;

public interface MyIngredientList {

    @FormUrlEncoded
    @PUT("ingredient_user")
    Call<String> getUserIngredientList(
            @Field("user_id") String UserId,
            @Field("add") String addIngredientList,
            @Field("delete") String deleteIngredientList
    );
}
