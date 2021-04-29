package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ValidateInterface {
    @FormUrlEncoded
    @POST("test/overlap/")
    Call<String> getUserValidate(
            @Field("user_id") String UserId
    );
}
