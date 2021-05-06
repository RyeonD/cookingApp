package com.example.frontapp;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginManager {
    private static final String PREF_USER_ID = "MyAutoLogin";

    public LoginManager() {

    }

    // 모든 엑티비티에서 인스턴스를 얻기위함
    static SharedPreferences getSharedPreferences(Context ctx) {
        // return PreferenceManager.getDefaultSharedPreferences(ctx);
        return ctx.getSharedPreferences(PREF_USER_ID, Context.MODE_PRIVATE);
    }

    // 계정 정보 저장 : 로그인 시 자동 로그인 여부에 따라 호출 될 메소드, 해당코드는  userId가 저장된다.
    public static void setUserId(Context ctx, String userId, boolean auto) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_ID, userId);
        editor.putBoolean("auto", auto);
        editor.commit();
    }

    // 저장된 정보 가져오기 : 현재 저장된 정보를 가져오기 위한 메소드
    public static String getUserId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_ID, "");
    }

    // 저장된 정보 가져오기 : 현재 저장된 정보를 가져오기 위한 메소드
    public static boolean getAuto(Context ctx) {
        return getSharedPreferences(ctx).getBoolean("auto", false);
    }

    // 로그아웃 : 자동 로그인 해제 및 로그아웃 시 호출 될 메소드
    public static void clearUserId(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.commit();
    }

}
