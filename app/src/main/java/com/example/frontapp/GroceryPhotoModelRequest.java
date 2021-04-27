package com.example.frontapp;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GroceryPhotoModelRequest extends StringRequest {

    private static String TAG = "PhotoRequest";
    final static private String URL = "http://6014494773b3.ngrok.io/test/yolov5/";
    private Map<String, String> map;

    public GroceryPhotoModelRequest(String imgFileName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        Log.e(TAG, "요청 생성 완료");
        map = new HashMap<>();
        map.put("bucket", "sagemaker-deploy-test");
        map.put("image_url", imgFileName);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        Log.e(TAG, "요청 리턴 완료");
        return map;
    }
}
