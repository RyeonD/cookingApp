package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginInterface
{
    String LOGIN_URL = "http://a7c6ab7c81d8.ngrok.io/";

    @FormUrlEncoded
    @POST("test/login/")
    Call<String> getUserLogin(
            @Field("user_id") String UserId,
            @Field("user_pw") String UserPwd
    );
}