package com.example.mymp3player;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Song implements Parcelable {
    private long id;
    private String data;
    private String title;
    private String artist;
    private String duration;
    private boolean isPlaying;

    public Song(long id, String data, String title, String artist, String duration) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.isPlaying = false;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        data = in.readString();
        title = in.readString();
        artist = in.readString();
        duration = in.readString();
        isPlaying = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(data);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(duration);
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }



    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                ", isPlaying=" + isPlaying +
                '}';
    }
}
