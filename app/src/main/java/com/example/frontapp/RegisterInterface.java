package com.example.frontapp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RegisterInterface {
    @FormUrlEncoded
    @POST("test/user/")
    Call<String> getUserRegist(
            @Field("user_id") String UserId,
            @Field("user_pw") String UserPwd,
            @Field("user_name") String UserName,
            @Field("userInfo") JSONObject UserInfo
    );
}
