package com.example.emosyncyt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.emosyncyt.R;
import com.example.emosyncyt.databinding.ActivityMainBinding;
import com.example.emosyncyt.ui.fragment.AccountFragment;
import com.example.emosyncyt.ui.fragment.HomeFragment;
import com.example.emosyncyt.ui.fragment.LibraryFragment;
import com.example.emosyncyt.ui.fragment.ShortsFragment;
import com.google.firebase.auth.FirebaseAuth;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
    private ImageView searchIcon;
    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private FirebaseAuth firebaseAuth;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity: ", "OpenCV is loaded successfully");
        } else {
            Log.d("MainActivity: ", "Failed to load OpenCV");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in; if not, redirect to LoginActivity
        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity
            return; // Prevent further execution of onCreate
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SearchView and SearchIcon
        searchView = findViewById(R.id.searchView);
        searchIcon = findViewById(R.id.search_icon);

        // Set up the SearchView to listen for queries
        setupSearchView();

        // Set the click listener for the search icon
        searchIcon.setOnClickListener(v -> {
            searchView.setVisibility(View.VISIBLE); // Show the search view
            searchIcon.setVisibility(View.GONE); // Hide the icon
        });

        // Set up bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                replaceFragment(homeFragment); // Only replace the fragment
            } else if (item.getItemId() == R.id.shorts) {
                replaceFragment(new ShortsFragment());
            } else if (item.getItemId() == R.id.account) {
                replaceFragment(new AccountFragment());
            } else if (item.getItemId() == R.id.library) {
                replaceFragment(new LibraryFragment());
            } else {
                return false;
            }
            return true;
        });

        // Load the HomeFragment initially
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            replaceFragment(homeFragment);
        }

        // Set up click listener for the FloatingActionButton
        binding.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent); // Start the CameraActivity
        });

        // Set up a callback for the back button using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Close search bar if visible when back button is pressed
                if (searchView.getVisibility() == View.VISIBLE) {
                    searchView.setVisibility(View.GONE); // Hide the search view
                    searchIcon.setVisibility(View.VISIBLE); // Show the search icon
                } else {
                    // If the search view is not visible, proceed with normal back press behavior
                    finish();
                }
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Pass the search query to HomeFragment
                if (homeFragment != null) {
                    homeFragment.searchForVideos(query);
                }
                searchView.setVisibility(View.GONE); // Hide the search view after submitting
                searchIcon.setVisibility(View.VISIBLE); // Show the icon again
                return true; // Indicate that the query has been handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // Optional: handle text changes if you want real-time search suggestions
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }
}
