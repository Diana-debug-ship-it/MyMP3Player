package com.example.mymp3player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayerActivity extends AppCompatActivity {

    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageView ivPlayNext, ivPlayPrev, ivPlayStop, ivMusicIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
    }

    private void initViews(){
        tvTitle = findViewById(R.id.song_title);
        tvArtist = findViewById(R.id.song_author);
        tvCurrentTime = findViewById(R.id.current_time);
        tvTotalTime = findViewById(R.id.total_time);

        seekBar = findViewById(R.id.seek_bar);
    }

}