package com.example.emosyncyt.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosyncyt.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;

    public SongAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.songTitle.setText(song.getTitle());
        Picasso.get().load(song.getThumbnailUrl()).into(holder.thumbnailImage);

        // Set click listener for song items
        holder.itemView.setOnClickListener(v -> listener.onSongClick(song.getSongUrl()));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        ImageView thumbnailImage;

        public SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.songTitle);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
        }
    }

    public static class Song {
        private String title;
        private String thumbnailUrl;
        private String songUrl;

        public Song(String title, String thumbnailUrl, String songUrl) {
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.songUrl = songUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public String getSongUrl() {
            return songUrl;
        }
    }

    public interface OnSongClickListener {
        void onSongClick(String songUrl);
    }
}
