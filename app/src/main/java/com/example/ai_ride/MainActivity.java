package com.example.ai_ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.ai_ride.screens.BusinessHome;
import com.example.ai_ride.screens.SelectRole;
import com.example.ai_ride.screens.UserHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is already authenticated
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is already logged in
            String userId = mAuth.getCurrentUser().getUid();
            checkUserRole(userId);
        } else {
            // User is not logged in, redirect to SelectRole activity
            redirectToRoleSelection();
        }
    }

    private void checkUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User exists in the Users node
                    startActivity(new Intent(MainActivity.this, UserHome.class));
                    finish();
                } else {
                    // User not found in the Users node, check in the Business node
                    checkBusinessRole(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                redirectToRoleSelection();
            }
        });
    }

    private void checkBusinessRole(String userId) {
        DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Business").child(userId);
        businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User exists in the Business node
                    startActivity(new Intent(MainActivity.this, BusinessHome.class));
                    finish();
                } else {
                    // User not found in either node, redirect to SelectRole activity
                    redirectToRoleSelection();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                redirectToRoleSelection();
            }
        });
    }

    private void redirectToRoleSelection() {
        // Redirect to SelectRole activity after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, SelectRole.class);
                startActivity(i);
                // Close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
