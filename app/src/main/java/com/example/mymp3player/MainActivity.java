package com.example.mymp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymp3player.adapters.SongDiffUtilCallback;
import com.example.mymp3player.adapters.SongsAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int TAG_SONGS_EQUAL = 1;
    public static final int TAG_SONGS_NOT_EQUAL = 0;

    private List<Song> songs;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private TextView tvNoMusicFound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadSongs();
        setupAdapter();

    }


    private void initViews() {
        tvNoMusicFound = findViewById(R.id.tvNoMusicFound);
    }

    private void setupAdapter() {
        if (songs.size() == 0) {
            tvNoMusicFound.setVisibility(View.VISIBLE);
        }
        songs.sort((o1, o2) -> {
            if (o1.getTitle().equals(o2.getTitle())) return TAG_SONGS_EQUAL;
            else return TAG_SONGS_NOT_EQUAL;
        });
        recyclerView = findViewById(R.id.rvSongs);
        adapter = new SongsAdapter(new SongDiffUtilCallback());
        adapter.submitList(songs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void loadSongs() {
        songs = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        Cursor musicCursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                songs.add(new Song(
                        musicCursor.getLong(idColumn),
                        musicCursor.getString(dataColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(durationColumn)));
            } while (musicCursor.moveToNext());
            musicCursor.close();
        }
        Log.d(Constants.TAG, songs.toString());
    }
}