package com.example.bemyvoice;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NavigationDestinationsActivity extends Activity {

    private static final int SPEECH_REQUEST_CODE = 1002;
    private static final float DESTINATION_PROXIMITY_THRESHOLD = 5; // Increased threshold for testing

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> allDestinations;
    private List<String> displayedDestinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_destination);

        String[] destinations = {"Room 1", "Room 2", "Room 3", "Room 4", "Room 5"};

        allDestinations = new ArrayList<>(Arrays.asList(destinations));
        displayedDestinations = new ArrayList<>(allDestinations);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedDestinations);
        listView.setAdapter(adapter);

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterDestinations(s.toString().trim().toLowerCase(Locale.getDefault()));
            }
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedDestination = (String) adapterView.getItemAtPosition(i);
            startNavigation(selectedDestination);
        });

        findViewById(R.id.voiceSearchButton).setOnClickListener(view -> startVoiceRecognition());

        // Search image button click listener
        ImageView searchImageButton = findViewById(R.id.searchIconButton);
        searchImageButton.setOnClickListener(v -> {
            String searchText = searchEditText.getText().toString().trim().toLowerCase(Locale.getDefault());
            filterDestinations(searchText);
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Your device doesn't support speech recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String query = result.get(0);
                startNavigation(query);
            }
        }
    }

    private void startNavigation(String destination) {
        // For simplicity, we simulate a destination location based on the selected destination name
        Location selectedLocation = new Location("");
        selectedLocation.setLatitude(37.7749); // Example latitude
        selectedLocation.setLongitude(-122.4194); // Example longitude

        // For testing purposes, set a mock user location within a certain radius of the destination
        Location userLocation = new Location("");
        userLocation.setLatitude(37.7750); // Example latitude within the testing radius
        userLocation.setLongitude(-122.4193); // Example longitude within the testing radius

        float distance = userLocation.distanceTo(selectedLocation);

        if (distance <= DESTINATION_PROXIMITY_THRESHOLD) {
            Toast.makeText(this, "You have arrived at " + destination, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, IndoorNavigationActivity.class);
            intent.putExtra("destination", destination);
            startActivity(intent);
            finish();
        }
    }

    private void filterDestinations(String searchText) {
        displayedDestinations.clear();
        if (searchText.isEmpty()) {
            displayedDestinations.addAll(allDestinations);
        } else {
            for (String destination : allDestinations) {
                if (destination.toLowerCase(Locale.getDefault()).contains(searchText)) {
                    displayedDestinations.add(destination);
                }
            }
        }
        adapter.notifyDataSetChanged();

        // Show or hide the "No matches found" TextView based on the filtered list
        TextView noMatchesTextView = findViewById(R.id.noMatchesTextView);
        if (displayedDestinations.isEmpty()) {
            noMatchesTextView.setVisibility(View.VISIBLE);
        } else {
            noMatchesTextView.setVisibility(View.GONE);
        }
    }
}
