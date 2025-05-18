package com.example.emosyncyt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.emosyncyt.R;
import com.example.emosyncyt.ui.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    private TextView textViewUsername;
    private TextView textViewEmail;
    private EditText editTextHappyUrl, editTextSadUrl, editTextSurpriseUrl, editTextFearUrl, editTextDisgustUrl, editTextNeuralUrl;
    private Button buttonLogout, buttonSaveUrls;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        editTextHappyUrl = view.findViewById(R.id.editTextHappyUrl);
        editTextSadUrl = view.findViewById(R.id.editTextSadUrl);
        editTextSurpriseUrl = view.findViewById(R.id.editTextSurpriseUrl);
        editTextFearUrl = view.findViewById(R.id.editTextFearUrl);
        editTextDisgustUrl = view.findViewById(R.id.editTextDisgustUrl);
        editTextNeuralUrl = view.findViewById(R.id.editTextNeuralUrl);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonSaveUrls = view.findViewById(R.id.buttonSaveUrls);

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Display user information
        displayUserInfo();

        // Set up logout button click listener
        buttonLogout.setOnClickListener(v -> logoutUser());

        // Set up save URLs button click listener
        buttonSaveUrls.setOnClickListener(v -> saveUserUrls());

        return view;
    }

    private void displayUserInfo() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Display email
            textViewEmail.setText(user.getEmail() != null ? user.getEmail() : "No email set");
            // Fetch and display username from Firestore
            fetchUsername(user.getUid());
            // Fetch user URLs
            fetchUserUrls(user.getUid());
        } else {
            textViewUsername.setText("No username set");
            textViewEmail.setText("No email available");
        }
    }

    private void fetchUsername(String userId) {
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    String username = task.getResult().getString("username"); // Assuming 'username' is the field name
                    textViewUsername.setText(username != null ? username : "No username set");
                } else {
                    textViewUsername.setText("No username set");
                }
            } else {
                Toast.makeText(getActivity(), "Failed to fetch username", Toast.LENGTH_SHORT).show();
                textViewUsername.setText("No username set");
            }
        });
    }

    private void fetchUserUrls(String userId) {
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    // Populate URL fields with existing data
                    editTextHappyUrl.setText(task.getResult().getString("happyurl"));
                    editTextSadUrl.setText(task.getResult().getString("sadurl"));
                    editTextSurpriseUrl.setText(task.getResult().getString("surpriseurl"));
                    editTextFearUrl.setText(task.getResult().getString("fearurl"));
                    editTextDisgustUrl.setText(task.getResult().getString("disgusturl"));
                    editTextNeuralUrl.setText(task.getResult().getString("neuralurl"));
                } else {
                    clearUrlFields();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to fetch URLs", Toast.LENGTH_SHORT).show();
                clearUrlFields();
            }
        });
    }

    private void clearUrlFields() {
        // Clear URL fields if no data is found
        editTextHappyUrl.setText("");
        editTextSadUrl.setText("");
        editTextSurpriseUrl.setText("");
        editTextFearUrl.setText("");
        editTextDisgustUrl.setText("");
        editTextNeuralUrl.setText("");
    }

    private void saveUserUrls() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.update(
                    "happyurl", editTextHappyUrl.getText().toString(),
                    "sadurl", editTextSadUrl.getText().toString(),
                    "surpriseurl", editTextSurpriseUrl.getText().toString(),
                    "fearurl", editTextFearUrl.getText().toString(),
                    "disgusturl", editTextDisgustUrl.getText().toString(),
                    "neuralurl", editTextNeuralUrl.getText().toString()
            ).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "URLs updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update URLs", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity after logout
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }
}
