package com.example.mymp3player;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Song implements Parcelable {
    private long id;
    private String title;
    private String artist;
    private Uri uri;
    private Uri artworkUri;
    private int duration;
    private int size;
    private boolean isPlaying;

    public Song(long id, String title, String artist, Uri uri, int duration, int size) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.duration = duration;
        this.size = size;
        this.artworkUri = null;
        this.isPlaying = false;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        artworkUri = in.readParcelable(Uri.class.getClassLoader());
        duration = in.readInt();
        size = in.readInt();
        isPlaying = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeParcelable(uri, flags);
        dest.writeParcelable(artworkUri, flags);
        dest.writeInt(duration);
        dest.writeInt(size);
        dest.writeByte((byte) (isPlaying ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id && duration == song.duration && size == song.size && isPlaying == song.isPlaying && Objects.equals(title, song.title) && Objects.equals(artist, song.artist) && Objects.equals(uri, song.uri) && Objects.equals(artworkUri, song.artworkUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, artist, uri, artworkUri, duration, size, isPlaying);
    }


}