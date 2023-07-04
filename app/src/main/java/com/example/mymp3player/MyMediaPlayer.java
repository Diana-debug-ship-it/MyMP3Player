package com.example.mymp3player;

import android.media.MediaPlayer;

public class MyMediaPlayer {

    private static MediaPlayer instance = null;

    public static MediaPlayer getInstance(){
        if (instance==null) {
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int currentIndex = -1;
}
