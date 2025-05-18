package com.example.emosyncyt.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosyncyt.data.network.Constants;
import com.example.emosyncyt.R;
import com.example.emosyncyt.data.source.RetrofitClient;
import com.example.emosyncyt.ui.adapter.VideoAdapter;
import com.example.emosyncyt.data.source.YouTubeApiService;
import com.example.emosyncyt.data.model.YouTubeResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int SEARCH_RESULTS_LIMIT = 5;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<YouTubeResponse.Item> allVideos = new ArrayList<>(); // List to cache all videos
    private int currentVideoIndex = 0; // Track the current index of videos displayed
    private boolean isLoading = false; // Loading state to prevent duplicate loads

    private TextView titleTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // Initialize adapter
        videoAdapter = new VideoAdapter(requireActivity(), new ArrayList<>());
        recyclerView.setAdapter(videoAdapter);

        // Check if we already have cached videos; if not, fetch them
        if (allVideos.isEmpty()) {
            fetchRandomVideos("Music"); // Fetch only if cache is empty
        } else {
            displayNextSetOfRandomVideos(); // Display cached videos
        }

        // Add scroll listener to the RecyclerView
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 1 && !isLoading) {
                    // User has scrolled to the bottom and not currently loading more videos
                    isLoading = true; // Set loading state
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            displayNextSetOfRandomVideos();
                            isLoading = false; // Reset loading state after loading
                        }
                    });
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resetVideoDisplay(); // Reset and display from the start of the cached list
    }

    public void fetchRandomVideos(String query) {
        YouTubeApiService apiService = RetrofitClient.getYouTubeApiService();
        Call<YouTubeResponse> call = apiService.searchVideos("snippet", query, Constants.API_KEY, 50);

        call.enqueue(new Callback<YouTubeResponse>() {
            @Override
            public void onResponse(Call<YouTubeResponse> call, Response<YouTubeResponse> response) {
                if (response.isSuccessful()) {
                    YouTubeResponse youTubeResponse = response.body();
                    if (youTubeResponse != null && youTubeResponse.getItems() != null) {
                        allVideos.clear(); // Clear any existing videos
                        allVideos.addAll(youTubeResponse.getItems()); // Cache fetched videos
                        Collections.shuffle(allVideos); // Shuffle to randomize the list

                        // Display the first 10 random videos
                        currentVideoIndex = 0; // Reset index
                        displayNextSetOfRandomVideos();

                        // Show toast indicating data has been fetched
                        Toast.makeText(getContext(), "Videos fetched successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No videos found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Request failed with error: " + response.message());
                    Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<YouTubeResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage());
                Toast.makeText(getContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayNextSetOfRandomVideos() {
        // Check if there are videos to display
        if (currentVideoIndex < allVideos.size()) {
            // Get the next 10 videos to display
            int endIndex = Math.min(currentVideoIndex + 10, allVideos.size());
            List<YouTubeResponse.Item> nextVideos = allVideos.subList(currentVideoIndex, endIndex);

            // Log the videos that will be displayed
            Log.d(TAG, "Displaying videos from index " + currentVideoIndex + " to " + endIndex);
            Log.d(TAG, "Next videos size: " + nextVideos.size());

            // Add videos to the adapter
            videoAdapter.addVideos(nextVideos);

            // Update the current index for the next set
            currentVideoIndex = endIndex; // Move the current index forward
        } else {
            // If we reached the end of the list, reset and display from the beginning
            Log.d(TAG, "Reached end of list, resetting.");
            currentVideoIndex = 0; // Reset the index
            displayNextSetOfRandomVideos(); // Display from the beginning
        }
    }

    public void resetVideoDisplay() {
        // Clear the adapter and reset current video index
        currentVideoIndex = 0;
        videoAdapter.clearVideos(); // Clear the current video list in the adapter

        // Check if allVideos is not empty before trying to display
        if (!allVideos.isEmpty()) {
            displayNextSetOfRandomVideos(); // Display the next set of videos from the cached list
        } else {
            Toast.makeText(getContext(), "No videos available to display!", Toast.LENGTH_SHORT).show();
        }
    }

    public void searchForVideos(String query) {
        YouTubeApiService apiService = RetrofitClient.getYouTubeApiService();
        Call<YouTubeResponse> call = apiService.searchVideos("snippet", query, Constants.API_KEY, SEARCH_RESULTS_LIMIT);

        call.enqueue(new Callback<YouTubeResponse>() {
            @Override
            public void onResponse(Call<YouTubeResponse> call, Response<YouTubeResponse> response) {
                if (response.isSuccessful()) {
                    YouTubeResponse youTubeResponse = response.body();
                    if (youTubeResponse != null && youTubeResponse.getItems() != null) {
                        List<YouTubeResponse.Item> searchResults = youTubeResponse.getItems();

                        // Update the adapter with search results
                        videoAdapter.updateVideos(searchResults);
                    } else {
                        Toast.makeText(getContext(), "No videos found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Request failed with error: " + response.message());
                    Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<YouTubeResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage());
                Toast.makeText(getContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
