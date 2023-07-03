package com.example.mymp3player;

import java.util.Objects;

public class Song {
    private long id;
    private String path;
    private String title;
    private String artist;
    private String duration;

    public Song(long id, String path, String title, String artist, String duration) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return id == song.id && Objects.equals(path, song.path) && Objects.equals(title, song.title) && Objects.equals(artist, song.artist) && Objects.equals(duration, song.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, title, artist, duration);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
