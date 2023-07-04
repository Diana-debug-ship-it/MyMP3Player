package com.example.mymp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.Selection;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int TAG_SONGS_EQUAL = 1;
    public static final int TAG_SONGS_NOT_EQUAL = 0;

    public static final String SERVICE_STATE = "ServiceState";

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.mymp3player.PlayNewAudio";

    private List<Song> songs;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private TextView tvNoMusicFound;

    private MediaPlayerService player;
    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvNoMusicFound = findViewById(R.id.tvNoMusicFound);

        songs = getSongs();
        if (songs.size() == 0) {
            tvNoMusicFound.setVisibility(View.VISIBLE);
        }
        songs.sort((o1, o2) -> {
            if (o1.getTitle().equals(o2.getTitle())) return TAG_SONGS_EQUAL;
            else return TAG_SONGS_NOT_EQUAL;
        });
        setupAdapter();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putBoolean(SERVICE_STATE, serviceBound);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean(SERVICE_STATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    private void setupAdapter() {
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
        if (musicCursor != null && musicCursor.moveToFirst()) {
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
            musicCursor.close();
        }
        return list;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio(int audioIndex) {
        if (!serviceBound) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio((ArrayList<Song>) songs);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null) {
            recyclerView.setAdapter(new SongsAdapter(new SongDiffUtilCallback()));
        }
    }
}