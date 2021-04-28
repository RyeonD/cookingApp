package com.example.frontapp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ValidateInterface {
    String REGIST_URL = "http://a7c6ab7c81d8.ngrok.io/";

    @FormUrlEncoded
    @POST("test/overlap/")
    Call<String> getUserValidate(
            @Field("user_id") String UserId
    );
}
