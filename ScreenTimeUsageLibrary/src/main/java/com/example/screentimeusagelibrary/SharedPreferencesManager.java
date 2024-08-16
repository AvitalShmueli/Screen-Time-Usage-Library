package com.example.screentimeusagelibrary;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static SharedPreferencesManager instance = null;
    private static final String DB_FILE = "DB_USAGE_FILE";
    private SharedPreferences sharedPref;

    private SharedPreferencesManager(Context context) {
        this.sharedPref = context.getSharedPreferences(DB_FILE, Context.MODE_PRIVATE);
    }


    public static void init(Context context) {
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                instance = new SharedPreferencesManager(context);
            }
        }
    }


    public static SharedPreferencesManager getInstance() {
        return instance;
    }


    public void putBoolean(String KEY, boolean value) {
        sharedPref.edit().putBoolean(KEY, value).apply();
    }


    public boolean getBoolean(String KEY, boolean defaultValue) {
        return sharedPref.getBoolean(KEY, defaultValue);
    }


    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    public int getInt(String key, int defaultValue) {
        return sharedPref.getInt(key, defaultValue);
    }


    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public long getLong(String key, long defaultValue) {
        return sharedPref.getLong(key, defaultValue);
    }


    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }


    public float getFloat(String key, float defaultValue) {
        return sharedPref.getFloat(key, defaultValue);
    }


    public void putFloat(String key, float value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(key, value);
        editor.apply();
    }


    public String getString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

}
