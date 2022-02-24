package com.harjeet.trackerever.MyUtils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class MySharedPref {
    public static void saveSharedValue(Context context,String key,String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSharedValue(Context context,String key) {
        SharedPreferences preferences=context.getSharedPreferences("myPref",MODE_PRIVATE);
        String value= preferences.getString(key,null);
        return value;
    }
}
