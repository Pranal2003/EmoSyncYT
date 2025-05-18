package com.example.emosyncyt.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emosyncyt.R;
import com.example.emosyncyt.ui.activity.VideoPlayerActivity;
import com.example.emosyncyt.data.model.YouTubeResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<YouTubeResponse.Item> videoList;
    private Set<String> videoIds = new HashSet<>(); // To store video IDs and avoid duplicates

    public VideoAdapter(Context context, List<YouTubeResponse.Item> videoList) {
        this.context = context;
        this.videoList = videoList;
        for (YouTubeResponse.Item video : videoList) {
            videoIds.add(video.getId().getVideoId()); // Initialize the set with the first batch of video IDs
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the video item layout
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        // Get the current video
        YouTubeResponse.Item video = videoList.get(position);

        // Set video title
        holder.titleTextView.setText(video.getSnippet().getTitle());

        // Load video thumbnail using Glide
        String thumbnailUrl = video.getSnippet().getThumbnails().getMedium().getUrl();
        Glide.with(context)
                .load(thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image) // Add a placeholder image
                .into(holder.thumbnailImageView);

        // Handle item click to start video playback
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", video.getId().getVideoId()); // Pass video ID to the player
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the video list
        return videoList != null ? videoList.size() : 0;
    }

    // Method to add new videos to the current list dynamically
    public void addVideos(List<YouTubeResponse.Item> newVideoList) {
        int startPosition = videoList.size();
        for (YouTubeResponse.Item video : newVideoList) {
            String videoId = video.getId().getVideoId();
            // Check if the video ID is already in the set
            if (!videoIds.contains(videoId)) {
                videoList.add(video);
                videoIds.add(videoId); // Add the video ID to the set to avoid duplicates
            }
        }
        notifyItemRangeInserted(startPosition, videoList.size() - startPosition); // Notify adapter about new items
    }

    // Method to clear the current video list
    public void clearVideos() {
        videoList.clear(); // Clear the list of videos
        videoIds.clear();  // Clear the set of video IDs
        notifyDataSetChanged(); // Notify adapter that the data has changed
    }

    // Method to update the current video list with new data (used for searches)
    public void updateVideos(List<YouTubeResponse.Item> newVideoList) {
        clearVideos(); // Clear current list and IDs before updating
        for (YouTubeResponse.Item video : newVideoList) {
            videoList.add(video);
            videoIds.add(video.getId().getVideoId()); // Add new videos
        }
        notifyDataSetChanged(); // Notify adapter that the data has changed
    }

    // ViewHolder for video items
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;

        VideoViewHolder(View itemView) {
            super(itemView);
            // Initialize views
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
        }
    }
}
