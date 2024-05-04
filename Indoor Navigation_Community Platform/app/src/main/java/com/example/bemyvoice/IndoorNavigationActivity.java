package com.example.bemyvoice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Locale;

public class IndoorNavigationActivity extends AppCompatActivity {

    private static final String TAG = "IndoorNavigationActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1003;
    private static final float DESTINATION_PROXIMITY_THRESHOLD = 2; // Threshold in meters

    private ArFragment arFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextToSpeech textToSpeech;
    private Location destinationLocation;
    private ModelRenderable arrowModelRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_navigation);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize AR scene
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return;
            }
            Anchor anchor = hitResult.createAnchor();
            placeArrowModel(anchor);
        });

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            loadArrowModel(); // Load arrow model if permission granted
        }

        // For testing purposes, set a mock destination location
        destinationLocation = new Location("");
        destinationLocation.setLatitude(37.7749); // Example latitude
        destinationLocation.setLongitude(-122.4194); // Example longitude
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void loadArrowModel() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///assets/model.obj"))
                .build()
                .thenAccept(renderable -> arrowModelRenderable = renderable)
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error loading arrow model: " + throwable.getMessage());
                    Toast.makeText(this, "Error loading arrow model", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadArrowModel();
            } else {
                Toast.makeText(this, "Camera permission is required for AR navigation.", Toast.LENGTH_SHORT).show();
                // Optionally, provide instructions on how to grant the permission manually
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Handle location permission result if needed
        }
    }


    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update interval in milliseconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location currentLocation = locationResult.getLastLocation();
                    updateLocation(currentLocation);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateLocation(Location currentLocation) {
        if (destinationLocation != null && currentLocation != null) {
            float distance = currentLocation.distanceTo(destinationLocation);

            String instructions = String.format(Locale.getDefault(),
                    "Walk %.1f meters to reach your destination.", distance);

            // Display navigation instructions
            displayNavigationInstructions(instructions);

            // Check if the user has reached within 2 meters of the destination
            if (distance <= DESTINATION_PROXIMITY_THRESHOLD) {
                // User has reached the destination
                onDestinationReached();
            } else {
                // Render AR objects
                renderARObjects(currentLocation);
            }
        }
    }

    private void displayNavigationInstructions(String instructions) {
        // Update UI with navigation instructions
    }

    private void renderARObjects(Location currentLocation) {
        if (arrowModelRenderable == null) {
            return;
        }

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            placeArrowModel(anchor);
        });

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(
                Pose.makeTranslation(0.0f, 0.0f, -1.0f)); // Adjust position if necessary
        placeArrowModel(anchor);
    }

    private void placeArrowModel(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode arrowNode = new TransformableNode(arFragment.getTransformationSystem());
        arrowNode.setParent(anchorNode);
        arrowNode.setRenderable(arrowModelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        arrowNode.select();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        stopLocationUpdates();
    }

    private void onDestinationReached() {
        Toast.makeText(this, "You have reached the destination!", Toast.LENGTH_SHORT).show();
        // Optionally, perform any actions when the destination is reached
    }
}
