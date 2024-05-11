package com.example.ai_ride.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Models.Business;
import com.example.ai_ride.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class BusinessSignup extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ProgressDialog progressDialog;

    private EditText ownerNameEditText, businessNameEditText, ownerEmailEditText, ownerStateEditText,
            ownerZipCodeEditText, ownerLocationEditText, ownerPhoneNumberEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
//    private ImageView circularImageView;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mBusinessDatabase;
    private boolean editProfile = false;
    String ownerName, businessName, ownerEmail, ownerState, ownerZipCode, ownerLocation, ownerPhoneNumber, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_signup);

        mAuth = FirebaseAuth.getInstance();
        mBusinessDatabase = FirebaseDatabase.getInstance().getReference("Business");
        TextView signupLogin = findViewById(R.id.signup_login);
        signupLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BusinessSignup.this, Login.class));
            }
        });

//        circularImageView = findViewById(R.id.circleImageView);
//        circularImageView.setOnClickListener(v -> openImageChooser());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating user...");
        progressDialog.setCancelable(true);


        ownerNameEditText = findViewById(R.id.signup_name);
        businessNameEditText = findViewById(R.id.business_name);
        ownerEmailEditText = findViewById(R.id.signup_email);
        ownerStateEditText = findViewById(R.id.signup_state);
        ownerZipCodeEditText = findViewById(R.id.signup_zip_code);
        ownerLocationEditText = findViewById(R.id.signup_location);
        ownerPhoneNumberEditText = findViewById(R.id.profile_phone);
        passwordEditText = findViewById(R.id.signup_pass);
        confirmPasswordEditText = findViewById(R.id.signup_confirm_pass);
        signUpButton = findViewById(R.id.sign_upp);

        editProfile = getIntent().getBooleanExtra("editProfile", false);
        if (editProfile) {
            retrieveUserData();
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateProfile();
                }
            });


            passwordEditText.setVisibility(View.GONE);
            confirmPasswordEditText.setVisibility(View.GONE);
            ownerEmailEditText.setVisibility(View.GONE);
//            FrameLayout frameLayout = findViewById(R.id.picture);
//            frameLayout.setVisibility(View.GONE);
            LinearLayout linearLayout = findViewById(R.id.login_now);
            linearLayout.setVisibility(View.GONE);

            signUpButton.setText("Update");
        } else {
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signUp();
                }
            });
        }
    }

    private void signUp() {
        ownerName = ownerNameEditText.getText().toString().trim();
        businessName = businessNameEditText.getText().toString().trim();
        ownerEmail = ownerEmailEditText.getText().toString().trim();
        ownerState = ownerStateEditText.getText().toString().trim();
        ownerZipCode = ownerZipCodeEditText.getText().toString().trim();
        ownerLocation = ownerLocationEditText.getText().toString().trim();
        ownerPhoneNumber = ownerPhoneNumberEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String imageUrl = "";
        if (imageUri != null) {
            imageUrl = imageUri.toString();
        }
        if (ownerName.isEmpty() || businessName.isEmpty() || ownerEmail.isEmpty() || ownerState.isEmpty() ||
                ownerZipCode.isEmpty() || ownerLocation.isEmpty() || ownerPhoneNumber.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty())  {
            Toast.makeText(BusinessSignup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

         if (password.length() < 6) {
             Toast.makeText(BusinessSignup.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return; // Exit the method
        }

        Business business = new Business(ownerName, businessName, ownerEmail, ownerState, ownerZipCode,
                ownerLocation, ownerPhoneNumber, password, imageUrl);
        createUser(business);
    }

    private void createUser(final Business business) {
        mAuth.createUserWithEmailAndPassword(business.getOwnerEmail(), business.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Send verification email
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Save business data to Firebase Database
                                                    saveBusinessData(business, firebaseUser.getUid());

                                                    Toast.makeText(BusinessSignup.this, "Registered successfully. Please verify your email.", Toast.LENGTH_LONG).show();
                                                    // Optionally redirect to login page or elsewhere
                                                    startActivity(new Intent(BusinessSignup.this, Login.class));
                                                    finish();
                                                } else {
                                                    // Error sending verification email
                                                    Toast.makeText(BusinessSignup.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(BusinessSignup.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void saveBusinessData(Business business, String userId) {
        mBusinessDatabase.child(userId).setValue(business)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(BusinessSignup.this, "Business data saved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(BusinessSignup.this, "Failed to save business data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            if (imageUri != null) {
                // Upload the image to Firebase Storage
                uploadImage(imageUri);
            }

            // Retrieve the existing Business object
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Business").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the existing Business object
                        Business business = dataSnapshot.getValue(Business.class);
                        if (business != null) {
                             business.setOwnerName(ownerNameEditText.getText().toString().trim());
                            business.setBusinessName(businessNameEditText.getText().toString().trim());
                            business.setOwnerState(ownerStateEditText.getText().toString().trim());
                            business.setOwnerZipCode(ownerZipCodeEditText.getText().toString().trim());
                            business.setOwnerLocation(ownerLocationEditText.getText().toString().trim());
                            business.setOwnerPhoneNumber(ownerPhoneNumberEditText.getText().toString().trim());

                             Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put("ownerName", business.getOwnerName());
                            updateMap.put("businessName", business.getBusinessName());
                            updateMap.put("ownerState", business.getOwnerState());
                            updateMap.put("ownerZipCode", business.getOwnerZipCode());
                            updateMap.put("ownerLocation", business.getOwnerLocation());
                            updateMap.put("ownerPhoneNumber", business.getOwnerPhoneNumber());

                             mBusinessDatabase.child(userId).updateChildren(updateMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(BusinessSignup.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(BusinessSignup.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(BusinessSignup.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BusinessSignup.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(BusinessSignup.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void retrieveUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Business")
                    .child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Business business = dataSnapshot.getValue(Business.class);
                        if (business != null) {
                            ownerNameEditText.setText(business.getOwnerName());
                            businessNameEditText.setText(business.getBusinessName());
                            ownerStateEditText.setText(business.getOwnerState());
                            ownerZipCodeEditText.setText(business.getOwnerZipCode());
                            ownerLocationEditText.setText(business.getOwnerLocation());
                            ownerPhoneNumberEditText.setText(business.getOwnerPhoneNumber());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
//    private void openImageChooser() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//    }
//
//    // Override onActivityResult to handle image selection result
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            circularImageView.setImageURI(imageUri);
//        }
//    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference imageRef = storageRef.child("profile_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the download URL
                        String imageUrl = uri.toString();
                        // Save the image URL to the database
                        saveImageUrl(imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle image upload failure
                });
    }

    // Method to save image URL to the database
    private void saveImageUrl(String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Business").child(userId);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("profileImageUrl", imageUrl);

            // Update the database with the image URL
            userRef.updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        // Image URL saved successfully
                        // You can show a success message or perform any other action here
                    })
                    .addOnFailureListener(e -> {
                        // Handle database update failure
                    });
        }
    }
}
