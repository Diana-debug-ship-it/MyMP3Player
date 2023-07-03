package com.example.mymp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Selection;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity{

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
        tvNoMusicFound = findViewById(R.id.tvNoMusicFound);

        songs = getSongs();
        if (songs.size()==0) {
            tvNoMusicFound.setVisibility(View.VISIBLE);
        }
        songs.sort((o1, o2) -> {
            if (o1.getTitle().equals(o2.getTitle())) return TAG_SONGS_EQUAL;
            else return TAG_SONGS_NOT_EQUAL;
        });
        setupAdapter();
    }

    private void setupAdapter(){
        recyclerView = findViewById(R.id.rvSongs);
        adapter = new SongsAdapter(new SongDiffUtilCallback());
        adapter.submitList(songs);
        adapter.setOnSongClickListener(new SongsAdapter.OnSongClickListener() {
            @Override
            public void onSongClick() {

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public List<Song> getSongs() {
        List<Song> list = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        Cursor musicCursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (musicCursor!=null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                list.add(new Song(
                        musicCursor.getLong(idColumn),
                        musicCursor.getString(dataColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(durationColumn)));
            } while (musicCursor.moveToNext());
        }
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView!=null) {
            recyclerView.setAdapter(new SongsAdapter(new SongDiffUtilCallback()));
        }
    }
}