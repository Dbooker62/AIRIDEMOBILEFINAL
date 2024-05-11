package com.example.ai_ride.screens;

import static com.example.ai_ride.Adapters.NotificationHelper.CHANNEL_ID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Adapters.NotificationHelper;
import com.example.ai_ride.Models.AddOfferModel;
import com.example.ai_ride.Models.User;
import com.example.ai_ride.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddOffer extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private static final int REQUEST_CODE_LOCATION = 1; // You can define it as any unique integer

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "OfferPrefs";

    //    private GoogleMap mMap;
//    private SupportMapFragment mMapView;
    private Button addLocationButton;
    private Button submit;
    private boolean isMapVisible = false;
    private EditText editTextTitle;
    private EditText addDescription;
    private Spinner selectCategory;
    private ImageButton addPhoto;
    private DatabaseReference mDatabase;
    private double latitude;
    private double longitude;
    TextView locationCoordinates;
    ProgressDialog progressDialog;

    private AddOfferModel offerToUpdate;
    private int positionToUpdate = -1;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference mStorageRef = storage.getReference();
    ImageButton back;

    private Uri selectedImageUri;
    private static final int NOTIFICATION_ID = 1;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save data to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", editTextTitle.getText().toString());
        editor.putString("description", addDescription.getText().toString());
        editor.putInt("categoryPosition", selectCategory.getSelectedItemPosition());
        // Save image URI as string
        if (selectedImageUri != null) {
            editor.putString("imageUri", selectedImageUri.toString());
        }
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

//        mMapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
//        mMapView.getView().setVisibility(View.GONE);

        back = findViewById(R.id.bck_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        locationCoordinates = findViewById(R.id.location_coordinates);

        Intent intent2 = getIntent();
        if (intent2 != null && intent2.hasExtra("latitude") && intent2.hasExtra("longitude")) {
            latitude = intent2.getDoubleExtra("latitude", 0.0);
            longitude = intent2.getDoubleExtra("longitude", 0.0);
            // Use latitude and longitude as needed
        }

        addLocationButton = findViewById(R.id.add_location);
        editTextTitle = findViewById(R.id.editTextTitle);
        addDescription = findViewById(R.id.add_description);
        selectCategory = findViewById(R.id.spinnerCategory);
        addPhoto = findViewById(R.id.add_photo);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOrUpdateOffer();
            }
        });


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Restore data from SharedPreferences
        String title = sharedPreferences.getString("title", "");
        String description = sharedPreferences.getString("description", "");
        int categoryPosition = sharedPreferences.getInt("categoryPosition", 0);
        String imageUriString = sharedPreferences.getString("imageUri", null);

        // Set the restored data to the UI
        editTextTitle.setText(title);
        addDescription.setText(description);
        selectCategory.setSelection(categoryPosition);
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString);
            Glide.with(this).load(selectedImageUri).into(addPhoto);
        }


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("offer")) {
            offerToUpdate = (AddOfferModel) intent.getSerializableExtra("offer");
            positionToUpdate = intent.getIntExtra("position", -1);

            if (offerToUpdate != null) {
                editTextTitle.setText(offerToUpdate.getTitle());
                addDescription.setText(offerToUpdate.getDescription());

                int categoryIndex = getCategoryIndex(offerToUpdate.getCategory());
                if (categoryIndex != -1) {
                    selectCategory.setSelection(categoryIndex);
                }
                Glide.with(this).load(offerToUpdate.getImageURL()).into(addPhoto);
            }
        }

        // Initialize map view
//        if (mMapView != null) {
//            mMapView.getMapAsync(this);
//        }

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(AddOffer.this, "", "Loading Map...", true);
                progressDialog.setCancelable(false);  // Disable cancellable by user interaction

                Intent intent = new Intent(AddOffer.this, MapActivity.class);

                startActivityForResult(intent, REQUEST_CODE_LOCATION);
                addLocationButton.setEnabled(false);  // Disable button to prevent multiple clicks

            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
    }

    private void addOrUpdateOffer() {
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Please select a location first", Toast.LENGTH_LONG).show();
            return;
        }

        // Get input values
        String title = editTextTitle.getText().toString().trim();
        String description = addDescription.getText().toString().trim();
        String category = selectCategory.getSelectedItem().toString().trim();

        // Check if a new image has been selected or use default
        String defaultImageUrl = "android.resource://" + getPackageName() + "/" + R.drawable.gift;
        Uri imageUri = selectedImageUri != null ? selectedImageUri : Uri.parse(defaultImageUrl);


        if (offerToUpdate != null) {
            updateOfferInFirebase(title, description, category, imageUri);
        } else {
            addOfferToFirebase(title, description, category, imageUri);
        }
    }

    protected void addOfferToFirebase(String title, String description, String category, Uri imageUri) {
        progressDialog = ProgressDialog.show(this, "Saving Offer", "Please wait...", true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        String userId = currentUser.getUid();

        StorageReference imageRef = mStorageRef.child("images/" + UUID.randomUUID().toString());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageURL = uri.toString();
                    AddOfferModel addOffer = new AddOfferModel(title, description, category, imageURL, latitude, longitude);
                    DatabaseReference offersRef = mDatabase.child("Business").child(userId).child("Offers");
                    String newOfferId = offersRef.push().getKey();
                    addOffer.setId(newOfferId);

                    offersRef.child(newOfferId).setValue(addOffer)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddOffer.this, "Offer added successfully", Toast.LENGTH_SHORT).show();
                                sendNotificationsForMatchingPreferences(category);
                                sendNotification(userId, category);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddOffer.this, "Failed to add offer", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddOffer.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    protected void updateOfferInFirebase(String title, String description, String category, Uri imageUri) {
        // Get the reference of the offer to be updated
        DatabaseReference offerRef = mDatabase.child("Business")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Offers")
                .child(offerToUpdate.getId());

            StorageReference imageRef = mStorageRef.child("images/" + UUID.randomUUID().toString());
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageURL = uri.toString();

                            // Update the offer with the new image URL
                            offerRef.child("title").setValue(title);
                            offerRef.child("description").setValue(description);
                            offerRef.child("category").setValue(category);
                            offerRef.child("imageURL").setValue(imageURL);

                            Toast.makeText(AddOffer.this, "Offer updated successfully", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(AddOffer.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
    }


    //  @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//         LatLng location = new LatLng(40.6892, -74.0445);
//        mMap.addMarker(new MarkerOptions().position(location).title("Marker Title"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
//    }
//
//    private void toggleMapVisibility() {
//        if (isMapVisible) {
//            mMapView.getView().setVisibility(View.GONE);
//            isMapVisible = false;
//        } else {
//            mMapView.getView().setVisibility(View.VISIBLE);
//            isMapVisible = true;
//        }
//    }

    private void openImagePicker() {
        // Check for READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(AddOffer.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddOffer.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
        } else {
            // Create an AlertDialog to let the user choose between Gallery or Files
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Image")
                    .setMessage("Choose your image source")
                    .setPositiveButton("Gallery", (dialog, which) -> {
                        // Intent to pick image from gallery
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
                    })
                    .setNegativeButton("Files", (dialog, which) -> {
                        // Intent to pick image from file manager
                        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        fileIntent.setType("image/*");
                        startActivityForResult(fileIntent, GALLERY_REQUEST_CODE);
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();  // Call openImagePicker again after permission is granted
        } else {
            Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();  // Dismiss the dialog if still showing
        }
        addLocationButton.setEnabled(true);  // Re-enable the button

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            addPhoto.setImageURI(selectedImageUri);
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            addPhoto.setImageResource(R.drawable.add_photo);
        } else if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            locationCoordinates.setText(String.format(Locale.getDefault(), "Lat: %.5f, Long: %.5f", latitude, longitude));

        }
    }

    private int getCategoryIndex(String category) {

        return -1;
    }



    private void sendNotificationsForMatchingPreferences(String category) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> matchingUsers = new ArrayList<>();

                // Iterate through each user in the Users node
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // Retrieve the user's preferences
                        String preferences = user.getPreferences().toString();

                        // Check if the user's preferences match the category
                        if (preferences != null && preferences.equals(category)) {
                            // If matched, add the user to the list
                            matchingUsers.add(userSnapshot.getKey());
                        }
                    }
                }

                // Send notifications to matching users
                for (String userId : matchingUsers) {
                    sendNotification(userId, category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void sendNotification(String userId, String category) {
        // Create notification content
        String notificationTitle = "New Offer in " + category;
        String notificationText = "Check out the latest offer in " + category;

        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.showNotification(notificationTitle, notificationText);
        createNotificationChannel();

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.add_photo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
     private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
