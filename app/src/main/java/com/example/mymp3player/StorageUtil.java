package com.example.mymp3player;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;

public class StorageUtil {

    public static final String EXTRA_ARRAY_LIST = "audioArrayList";
    public static final String EXTRA_AUDIO_INDEX = "audioIndex";
    public final String STORAGE = "com.example.mymp3player.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Song> arrayList){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(EXTRA_ARRAY_LIST, json);
        editor.apply();
    }

    public ArrayList<Song> loadAudio(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = preferences.getString(EXTRA_ARRAY_LIST, null);
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(EXTRA_AUDIO_INDEX, index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt(EXTRA_AUDIO_INDEX, -1);
    }

    public void clearCachedAudioPlaylist(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
