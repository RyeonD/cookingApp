package com.example.frontapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ValidateRequest extends StringRequest {

    // 서버 URL 설정
    final static  private String URL="http://6014494773b3.ngrok.io/test/overlap/";
    private Map<String, String> map;

    public ValidateRequest(String UserId, Response.Listener<String> listener){
        super(Method.POST, URL, listener,null);

        map = new HashMap<>();
        map.put("user_id", UserId);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}