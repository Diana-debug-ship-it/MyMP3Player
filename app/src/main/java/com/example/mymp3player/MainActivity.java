package com.example.mymp3player;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymp3player.adapters.SongDiffUtilCallback;
import com.example.mymp3player.adapters.SongsAdapter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


enum RepeatMode {
    NOT_REPEAT,
    REPEAT_ALL,
    REPEAT_ONE
}

public class MainActivity extends AppCompatActivity {


    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageView ivPlayNext, ivPlayPrev, ivPlayStop, ivMusicIcon,
    ivRepeat, ivPlaylist;


    private List<Song> songs;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private TextView tvNoMusicFound;
    private TextView tvStartPlaying;
    private TextView tvShuffle;
    private ExoPlayer exoPlayer;

    private RepeatMode mode = RepeatMode.NOT_REPEAT;

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

        exoPlayer = new ExoPlayer.Builder(this).build();
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

        if (songs.size() > 0) {
            for (Song song : songs) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(song);
                }
            }

            if (adapter != null) {
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
        adapter = new SongsAdapter(new SongDiffUtilCallback(), exoPlayer);
        adapter.submitList(songs);
        //recyclerView.setAdapter(adapter);


        //recyclerview animators adapter
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnSongClickListener(new SongsAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(View view, int index) {
                if (!exoPlayer.isPlaying()) {
                    exoPlayer.setMediaItems(getMediaItems(), index, 0);
                } else {
                    exoPlayer.pause();
                    exoPlayer.seekTo(index, 0);
                }
                exoPlayer.prepare();
                exoPlayer.play();

                Toast.makeText(MainActivity.this, songs.get(index).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer.isPlaying()) {
            exoPlayer.stop();
        } else {
            exoPlayer.release();
        }
    }

    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    private void loadSongs() {
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


    private void initPlayerViews(){
        tvTitle = findViewById(R.id.song_title);
        tvArtist = findViewById(R.id.song_author);
        tvCurrentTime = findViewById(R.id.current_time);
        tvTotalTime = findViewById(R.id.total_time);

        seekBar = findViewById(R.id.seek_bar);

        ivPlayNext = findViewById(R.id.next);
        ivPlayPrev = findViewById(R.id.previous);
        ivPlayStop = findViewById(R.id.pause_play);
        ivMusicIcon = findViewById(R.id.music_icon_big);
        ivRepeat = findViewById(R.id.repeat);
        ivPlaylist = findViewById(R.id.playlist);
    }

}