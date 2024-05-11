package com.example.ai_ride.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ai_ride.Models.User;
import com.example.ai_ride.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSignup extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView circularImageView;
    private Uri imageUri;
    private EditText nameEditText, emailEditText, stateEditText, zipCodeEditText, phoneEditText, passwordEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7, checkBox8, checkBox9, checkBox10, checkBox11, checkBox12;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");


        circularImageView = findViewById(R.id.circleImageView);
        circularImageView.setOnClickListener(v -> openImageChooser());

        nameEditText = findViewById(R.id.signup_name);
        emailEditText = findViewById(R.id.signup_email);
        stateEditText = findViewById(R.id.signup_state);
        zipCodeEditText = findViewById(R.id.signup_zip_code);
        phoneEditText = findViewById(R.id.profile_phone);
        passwordEditText = findViewById(R.id.signup_pass);
        signUpButton = findViewById(R.id.sign_upp);
        checkBox1 = findViewById(R.id.checkBoxRestaurant);
        checkBox2 = findViewById(R.id.checkBoxCar);
        checkBox3= findViewById(R.id.checkHouseholdItems);
        checkBox4 = findViewById(R.id.checkBoxGuestrooms);
        checkBox5= findViewById(R.id.checkBoxHotel);
        checkBox6= findViewById(R.id.checkBoxFurniture);
        checkBox7= findViewById(R.id.checkBoxWholesale);
        checkBox8= findViewById(R.id.checkBoxTextTexttile);
        checkBox9= findViewById(R.id.checkBoxServices);
        checkBox10= findViewById(R.id.checkBoxEducation);
        checkBox11= findViewById(R.id.checkBoxArtifacts);
        checkBox12= findViewById(R.id.checkBoxOthers);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating user...");
        progressDialog.setCancelable(true);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String zipCode = zipCodeEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        List<String> preferencesList = new ArrayList<>();
        if (checkBox1.isChecked()) {
            preferencesList.add("Restaurant");
        }
        if (checkBox2.isChecked()) {
            preferencesList.add("Car");
        }
        if (checkBox3.isChecked()) {
            preferencesList.add("Household Items");
        }
        if (checkBox4.isChecked()) {
            preferencesList.add("Guestrooms");
        }
        if (checkBox5.isChecked()) {
            preferencesList.add("Hotel");
        }
        if (checkBox6.isChecked()) {
            preferencesList.add("Furniture");
        }
        if (checkBox7.isChecked()) {
            preferencesList.add("Wholesale");
        }
        if (checkBox8.isChecked()) {
            preferencesList.add("Texttile");
        }
        if (checkBox9.isChecked()) {
            preferencesList.add("Services");
        }
        if (checkBox10.isChecked()) {
            preferencesList.add("Education");
        }
        if (checkBox11.isChecked()) {
            preferencesList.add("Artifacts");
        }
        if (checkBox12.isChecked()) {
            preferencesList.add("Other");
        }
        if (!checkBox1.isChecked() && !checkBox2.isChecked() && !checkBox3.isChecked() && !checkBox4.isChecked() && !checkBox5.isChecked() && !checkBox6.isChecked() && !checkBox7.isChecked() && !checkBox8.isChecked() && !checkBox9.isChecked() && !checkBox10.isChecked() && !checkBox11.isChecked() && !checkBox12.isChecked()) {
            Toast.makeText(UserSignup.this, "Select at least one preference", Toast.LENGTH_SHORT).show();
            return;
        }
        String imageUrl = "";
        if (imageUri != null) {
            imageUrl = imageUri.toString();
        } else {
            Toast.makeText(UserSignup.this, "", Toast.LENGTH_SHORT).show();
        }

        if (name.isEmpty() || email.isEmpty() || state.isEmpty() || zipCode.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(UserSignup.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(UserSignup.this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> preferences = new ArrayList<>();
        // Add preferences here if needed

        User user = new User(name, email, state, zipCode, imageUrl, phoneNumber, password, preferencesList);
        progressDialog.show();
        createUser(user);
    }

    private void createUser(final User user) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                writeInitialUserData(firebaseUser.getUid(), true);  // true or false depending on default opt-in or opt-out

                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Email sent for verification
                                                    String userId = firebaseUser.getUid();
                                                    Map<String, Object> userData = new HashMap<>();
                                                    userData.put("name", user.getName());
                                                    userData.put("email", user.getEmail());
                                                    userData.put("state", user.getState());
                                                    userData.put("zipCode", user.getZipCode());
                                                    userData.put("phoneNumber", user.getPhoneNumber());
                                                    userData.put("profileImageUrl", user.getUserImage());
                                                    userData.put("preferences", user.getPreferences());

                                                    mDatabase.child(userId).setValue(userData);
                                                    progressDialog.dismiss();
                                                    Toast.makeText(UserSignup.this, "Registered successfully. Please check your email for verification.", Toast.LENGTH_LONG).show();
                                                    finish();
                                                } else {
                                                    // Error in sending verification email
                                                    Toast.makeText(UserSignup.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(UserSignup.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            circularImageView.setImageURI(imageUri);
        }
    }


    private void writeInitialUserData(String userId, boolean defaultNotificationSetting) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("notificationsEnabled", defaultNotificationSetting);
        userRef.updateChildren(userData);
    }

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

     private void saveImageUrl(String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("userImage", imageUrl);

            // Update the database with the image URL
            userRef.updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        // Handle database update failure
                    });
        }
    }
}
