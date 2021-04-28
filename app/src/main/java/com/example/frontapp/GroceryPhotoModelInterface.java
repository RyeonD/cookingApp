package com.example.frontapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GroceryPhotoModelInterface {
    String Model_URL = "http://e43959c95006.ngrok.io/";

    @FormUrlEncoded
    @POST("test/yolov5/")
    Call<String> getModelResult(
            @Field("bucket") String bucketName,
            @Field("image_url") String imgFileName
    );
}
