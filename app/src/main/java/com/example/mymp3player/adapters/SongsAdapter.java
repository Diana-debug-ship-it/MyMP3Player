package com.example.mymp3player.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymp3player.Constants;
import com.example.mymp3player.R;
import com.example.mymp3player.Song;
import com.google.android.exoplayer2.ExoPlayer;

public class SongsAdapter extends ListAdapter<Song, SongsAdapter.SongsViewHolder> {

    private OnSongClickListener onSongClickListener;
    private ExoPlayer player;

    public void setOnSongClickListener(OnSongClickListener onSongClickListener) {
        this.onSongClickListener = onSongClickListener;
    }

    public SongsAdapter(SongDiffUtilCallback diffCallback, ExoPlayer exoPlayer) {
        super(diffCallback);
        this.player = exoPlayer;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutSource;
        if (viewType == Constants.TYPE_ACTIVE) layoutSource = R.layout.item_song_active;
        else if (viewType == Constants.TYPE_INACTIVE) layoutSource = R.layout.item_song_disabled;
        else throw new RuntimeException("view type is absent");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutSource,
                        parent,
                        false);
        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        Song song = getItem(position);
        holder.tvName.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        Uri artwork = song.getArtworkUri();
        if (artwork!=null) {
            holder.ivArtwork.setImageURI(artwork);
            if (holder.ivArtwork.getDrawable()==null) {
                holder.ivArtwork.setImageResource(R.drawable.cat);
            }
        }

        int finalPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSongClickListener != null) {
                    onSongClickListener.onSongClick(holder.itemView, finalPosition);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isPlaying()) return Constants.TYPE_ACTIVE;
        else return Constants.TYPE_INACTIVE;
    }

    public class SongsViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvArtist;
        private ImageView ivArtwork;

        public SongsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivArtwork = itemView.findViewById(R.id.ivArtwork);
        }
    }

    public interface OnSongClickListener {
        void onSongClick(View view, int index);
    }
}
