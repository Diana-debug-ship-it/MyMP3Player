package com.example.mymp3player.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.mymp3player.Song;

public class SongDiffUtilCallback extends DiffUtil.ItemCallback<Song> {
    @Override
    public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
        return oldItem.getId()==newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
        return oldItem.equals(newItem);
    }
}
