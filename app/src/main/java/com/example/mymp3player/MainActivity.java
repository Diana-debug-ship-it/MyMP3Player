package com.example.mymp3player;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymp3player.adapters.SongDiffUtilCallback;
import com.example.mymp3player.adapters.SongsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private List<Song> songs;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private TextView tvNoMusicFound;
    private TextView tvStartPlaying;
    private TextView tvShuffle;

    private String[] projection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        initViews();
        loadSongs();
        setupAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_button);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchSong(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    private void searchSong(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();

        if (songs.size()>0) {
            for (Song song: songs) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(song);
                }
            }

            if (adapter!=null) {
                adapter.submitList(filteredList);
            }
        }
    }

    private void initViews() {
        tvNoMusicFound = findViewById(R.id.tvNoMusicFound);
        tvShuffle = findViewById(R.id.ivShuffle);
        tvStartPlaying = findViewById(R.id.ivPlay);

        tvShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.shuffle(songs);
                //это надо как-то исправить но пока что будет так
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupAdapter() {
        if (songs.size() == 0) {
            tvNoMusicFound.setVisibility(View.VISIBLE);
        }
        songs.sort((o1, o2) -> {
            if (o1.getTitle().equals(o2.getTitle())) return Constants.TAG_SONGS_EQUAL;
            else return Constants.TAG_SONGS_NOT_EQUAL;
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
            int sizeColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            long albumId = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            do {
                Song song = new Song(
                        musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicCursor.getLong(idColumn)),
                        musicCursor.getInt(durationColumn),
                        musicCursor.getInt(sizeColumn));
                song.setArtworkUri(ContentUris.withAppendedId(Uri.parse(Constants.URI_ARTWORK), albumId));

                songs.add(song);

            } while (musicCursor.moveToNext());
            musicCursor.close();
        }
        Log.d(Constants.TAG, songs.toString());
    }
}