package com.example.groupcycling;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static MySharedPreferences instance;

    private MySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static MySharedPreferences getInstance(Context context){
        if(instance == null){
            instance = new MySharedPreferences(context);
        }

        return instance;
    }

    public void addMyName(String s){
        editor.putString("name", s);
        editor.apply();
    }

    public String getMyName(){
        return sharedPreferences.getString("name", "");
    }

    public void addMyUid(String s){
        editor.putString("uid", s);
        editor.apply();
    }

    public String getMyUid(){
        return sharedPreferences.getString("uid", "");
    }

    public void addToken(String token){
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken(){
        return sharedPreferences.getString("token", "");
    }
}
