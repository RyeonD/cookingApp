package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MainInterface {
    @FormUrlEncoded
    @POST("user_ingredient_all")
    Call<String> getUserId(
            @Field("user_id") String UserId
    );
}
