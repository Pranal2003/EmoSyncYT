package com.example.emosyncyt.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosyncyt.R;
import com.example.emosyncyt.ui.adapter.SongAdapter;
import com.example.emosyncyt.data.network.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaySongActivity extends AppCompatActivity {

    private String detectedEmotion;
    private FirebaseFirestore firestore;
    private String happyUrl, sadUrl, surpriseUrl, fearUrl, disgustUrl, neuralUrl;

    private RecyclerView songRecyclerView;
    private SongAdapter songAdapter;
    private List<SongAdapter.Song> songList; // Holds video titles and thumbnails for RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        TextView emotionTextView = findViewById(R.id.emotionTextView); // TextView to display the emotion
        Button playSongButton = findViewById(R.id.playSongButton); // Button to play the song
        songRecyclerView = findViewById(R.id.songRecyclerView); // RecyclerView to display songs

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(songList, new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(String songUrl) {
                // Redirect to the song URL when a song is clicked
                redirectToSong(songUrl);
            }
        });
        songRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songRecyclerView.setAdapter(songAdapter);

        // Get the detected emotion passed from CameraActivity
        detectedEmotion = getIntent().getStringExtra("detectedEmotion");
        emotionTextView.setText("Detected Emotion: " + detectedEmotion);

        // Fetch song URIs from Firestore
        fetchUserSongUris();

        // Fetch YouTube videos based on emotion
        if (detectedEmotion != null) {
            fetchYouTubeVideos(detectedEmotion);
        }

        // Set up the play song button click listener
        playSongButton.setOnClickListener(v -> redirectToSong(getSongUriByEmotion()));
    }

    private void fetchUserSongUris() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        happyUrl = task.getResult().getString("happyurl");
                        sadUrl = task.getResult().getString("sadurl");
                        surpriseUrl = task.getResult().getString("surpriseurl");
                        fearUrl = task.getResult().getString("fearurl");
                        disgustUrl = task.getResult().getString("disgusturl");
                        neuralUrl = task.getResult().getString("neuralurl");
                    } else {
                        Toast.makeText(this, "No song URLs found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to fetch song URLs", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchYouTubeVideos(String emotion) {
        // Define the search query based on emotion
        String query = emotion + " song";
        String url = Constants.BASE_URL + "search?part=snippet&maxResults=5&q="
                + query + "&key=" + Constants.API_KEY;

        // Perform HTTP request to fetch data from YouTube
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseYouTubeResponse(responseData);
                } else {
                    runOnUiThread(() -> Toast.makeText(PlaySongActivity.this, "Failed to fetch videos", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PlaySongActivity.this, "Error occurred while fetching videos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseYouTubeResponse(String responseData) {
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);
        List<SongAdapter.Song> fetchedSongs = new ArrayList<>();

        // Parse the response and extract video titles and thumbnails
        JsonArray items = jsonResponse.getAsJsonArray("items");

        // Limit to 5 items or the available number of items
        int maxSongsToDisplay = Math.min(5, items.size());

        for (int i = 0; i < maxSongsToDisplay; i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");

            // Safely retrieve the title and thumbnailUrl
            String title = snippet != null && snippet.has("title") ? snippet.get("title").getAsString() : "Unknown Title";
            String thumbnailUrl = snippet != null && snippet.has("thumbnails") ? snippet.getAsJsonObject("thumbnails")
                    .getAsJsonObject("medium").get("url").getAsString() : "https://via.placeholder.com/150"; // Fallback image

            // Safely retrieve video URL (ensure "id" exists and "videoId" is valid)
            String videoId = item.getAsJsonObject().getAsJsonObject("id").has("videoId") ?
                    item.getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString() : null;
            String videoUrl = (videoId != null) ? "https://www.youtube.com/watch?v=" + videoId : "";

            // Add the song to the list if the videoUrl is valid
            if (!videoUrl.isEmpty()) {
                fetchedSongs.add(new SongAdapter.Song(title, thumbnailUrl, videoUrl));
            }
        }

        // Update the RecyclerView on the main thread
        runOnUiThread(() -> {
            if (songList != null) {
                Log.d("FetchedSongs", "Number of songs fetched: " + fetchedSongs.size());
                songList.clear();
                songList.addAll(fetchedSongs);
                songAdapter.notifyDataSetChanged();
            }
        });
    }



    private void redirectToSong(String songUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(songUrl));
        startActivity(intent);
    }

    private String getSongUriByEmotion() {
        String songUri;

        // Determine the song URI based on the detected emotion
        switch (detectedEmotion != null ? detectedEmotion : "default") {
            case "happy":
                songUri = happyUrl != null ? happyUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
            case "sad":
                songUri = sadUrl != null ? sadUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
            case "surprise":
                songUri = surpriseUrl != null ? surpriseUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
            case "fear":
                songUri = fearUrl != null ? fearUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
            case "disgust":
                songUri = disgustUrl != null ? disgustUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
            default:
                songUri = neuralUrl != null ? neuralUrl : "https://youtu.be/AETFvQonfV8?feature=shared"; // Replace with actual default URI
                break;
        }
        return songUri;
    }
}
