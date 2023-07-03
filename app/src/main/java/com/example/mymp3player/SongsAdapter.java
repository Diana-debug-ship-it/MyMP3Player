package com.example.mymp3player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class SongsAdapter extends ListAdapter<Song, SongsAdapter.SongsViewHolder> {

    private OnSongClickListener onSongClickListener;

    public void setOnSongClickListener(OnSongClickListener onSongClickListener) {
        this.onSongClickListener = onSongClickListener;
    }

    protected SongsAdapter(SongDiffUtilCallback diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_active,
                        parent,
                        false);
        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        Song song = getItem(position);
        holder.tvName.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSongClickListener!=null) {
                    onSongClickListener.onSongClick();
                }
            }
        });
    }

    public class SongsViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvArtist;

        public SongsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvArtist = itemView.findViewById(R.id.tvArtist);
        }
    }

    public interface OnSongClickListener{
        void onSongClick();
    }
}
