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

    public Song(long id, String data, String title, String artist, String duration) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        data = in.readString();
        title = in.readString();
        artist = in.readString();
        duration = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(data);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(duration);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id && Objects.equals(data, song.data) && Objects.equals(title, song.title) && Objects.equals(artist, song.artist) && Objects.equals(duration, song.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, title, artist, duration);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
