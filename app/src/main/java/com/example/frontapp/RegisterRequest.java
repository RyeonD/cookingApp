package com.example.frontapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    //서버 URL 설정
    final static private String URL = "http://6014494773b3.ngrok.io/test/user/";
    private Map<String, String> map;
    private Map<String, String> info_map;
    //private Map<String, String>parameters;

    public RegisterRequest(String UserId, String UserEmail, String UserPwd, String UserName, String Address, String Phone, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        info_map = new HashMap<>();
        info_map.put("phone", Phone);
        info_map.put("email", UserEmail);
        info_map.put("address", Address);
        info_map.put("gender", "남");
        JSONObject json = new JSONObject(info_map);
        map = new HashMap<>();
        map.put("user_id", UserId);
        map.put("user_pw", UserPwd);
        map.put("user_name", UserName);
        map.put("userInfo", json.toString());
//        map.put("UserPhone", Phone);
//        map.put("UserAddress", Address);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}