package com.example.frontapp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GroceryListInPhotoInterface {
    //    @FormUrlEncoded
    @Headers("Content-Type: application/json; charset=utf8")
    @POST("prod/")
    Call<String> getModelResult(
            @Body JSONObject input
//            @Field("bucket") String bucketName,
//            @Field("image_url") String imgFileName
    );
}
