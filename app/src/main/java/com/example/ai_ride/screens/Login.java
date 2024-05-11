package com.example.ai_ride.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class Login extends AppCompatActivity {
    private String role;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    TextView forget_pass;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        forget_pass = findViewById(R.id.forget_pswd);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);

        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(true);

        SharedPreferences sharedPreferences = getSharedPreferences("UserRole", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the role from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            role = extras.getString("role");
            editor.putString("role", role); // Save the role to shared preferences
        }

        editor.apply();


        forget_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgetPassword.class));
            }
        });
    }

    public void onLogin(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the ProgressDialog
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false); // Make the dialog not cancellable
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Email is verified, proceed with login
                                String userId = user.getUid();
                                checkUserRole(userId);
                            } else {
                                // Email is not verified, prompt the user
                                Toast.makeText(Login.this, "Please verify your email to log in.", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss(); // Dismiss dialog if verification needed
                            }
                        } else {
                            // Authentication failed, show error message
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(Login.this, "Authentication failed. " + errorMessage, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss(); // Dismiss dialog on failure
                        }
                    }
                });
    }

    private void checkUserRole(String userId) {
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Navigate to User Home
                    progressDialog.dismiss(); // Dismiss dialog before navigation
                    startActivity(new Intent(Login.this, UserHome.class));
                    finish();
                } else {
                    mDatabase.child("Business").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Navigate to Business Home
                                progressDialog.dismiss(); // Dismiss dialog before navigation
                                startActivity(new Intent(Login.this, BusinessHome.class));
                                finish();
                            } else {
                                // User not found in either node, redirect to user or business signup based on role
                                progressDialog.dismiss(); // Dismiss dialog before navigation
                                startActivity(new Intent(Login.this, SelectRole.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Login.this, "Database error", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss(); // Ensure dialog is dismissed even on database error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Database error", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss(); // Ensure dialog is dismissed even on database error
            }
        });
    }

    public void navigateToRegistration(View view) {
        Intent intent;
        if ("user".equals(role)) {
            intent = new Intent(Login.this, UserSignup.class);
        } else if ("business".equals(role)) {
            intent = new Intent(Login.this, BusinessSignup.class);
        } else {
            intent = new Intent(Login.this, UserSignup.class);
        }
        startActivity(intent);
    }

}
