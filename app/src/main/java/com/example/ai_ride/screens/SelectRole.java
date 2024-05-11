package com.example.ai_ride.screens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ai_ride.R;

public class SelectRole extends AppCompatActivity {
    private Button buttonUser;
    private Button buttonBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        buttonUser = findViewById(R.id.buttonUser);
        buttonBusiness = findViewById(R.id.buttonBusiness);

        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoleAndNavigate("user");
            }
        });

        buttonBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoleAndNavigate("business");
            }
        });
    }

    private void saveRoleAndNavigate(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserRole", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("role", role);
        editor.apply();

        navigateToLogin(role);
    }

    private void navigateToLogin(String role) {
        Intent intent = new Intent(SelectRole.this, Login.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
