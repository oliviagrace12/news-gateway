package com.example.newsgateway;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

class MyProjectSharedPreference {

    private static final String TAG = "MyProjectSharedPreference";
    private SharedPreferences prefs;

    MyProjectSharedPreference(Activity activity) {
        super();
        prefs = activity.getSharedPreferences(activity.getString(R.string.prefsFileKey),
                Context.MODE_PRIVATE);
    }

    void save(String key, String text) {
        Log.d(TAG, "save: " + key + ":" + text);
        Editor editor = prefs.edit();
        editor.putString(key, text);
        editor.apply(); // commit T/F
    }


    String getValue(String key) {
        String text = prefs.getString(key, "");
        Log.d(TAG, "getValue: " + key + " = " + text);
        return text;
    }

    boolean hasValue(String key) {
        return prefs.contains(key);
    }

    void clearAll() {
        Log.d(TAG, "clearAll: ");
        Editor editor = prefs.edit();
        editor.clear();
        editor.apply(); // commit T/F
    }

    void removeValue(String key) {
        Log.d(TAG, "removeValue: " + key);
        Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply(); // commit T/F
    }
}