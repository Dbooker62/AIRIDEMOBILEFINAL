package com.example.ai_ride.screens;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Adapters.UserAdapter;
import com.example.ai_ride.Models.UserOfferModel;
import com.example.ai_ride.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserHome extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserOfferModel> offerList;
    private List<String> userPreferences;

    Button seeAll;
    ImageView userProfile;
    MapView dialogMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        userProfile = findViewById(R.id.user_profile);
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserHome.this, UserProfile.class));
            }
        });

        recyclerView = findViewById(R.id.user_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seeAll = findViewById(R.id.seeAllOffers);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserHome.this, SeeAllOffers.class));
            }
        });

        offerList = new ArrayList<>();
        adapter = new UserAdapter(this, offerList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserOfferModel offer) {
                showOfferDetailsDialog(offer);
            }
        });

        fetchUserPreferences();
    }

    private void fetchUserPreferences() {
         DatabaseReference userPreferencesRef = FirebaseDatabase.getInstance().getReference("Users").child(getCurrentUserId()).child("preferences");
        userPreferencesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPreferences = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String preference = snapshot.getValue(String.class);
                    if (preference != null) {
                        userPreferences.add(preference);
                    }
                }
                 fetchOffers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserHome.this, "Failed to fetch user preferences", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOffers() {
        DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference("Business");

        businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot businessSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot offersSnapshot = businessSnapshot.child("Offers");
                    for (DataSnapshot offerSnapshot : offersSnapshot.getChildren()) {
                        UserOfferModel offer = offerSnapshot.getValue(UserOfferModel.class);
                        if (offer != null && isOfferMatchingPreference(offer)) {
                            offerList.add(offer);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserHome.this, "Failed to fetch offers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOfferMatchingPreference(UserOfferModel offer) {
        // Check if the offer's category matches any of the user's preferences
        return userPreferences.contains(offer.getCategory());
    }


     private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();

        }
        return null;
    }



    private void showOfferDetailsDialog(UserOfferModel offer) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.offer_dialog);

        ImageView dialogImageView = dialog.findViewById(R.id.dialogImageView);
        TextView dialogTitleTextView = dialog.findViewById(R.id.dialogTitleTextView);
        TextView dialogSubtitleTextView = dialog.findViewById(R.id.dialogSubtitleTextView);
        TextView dialogCategoryTextView = dialog.findViewById(R.id.dialogCategoryTextView);

        Button closeButton = dialog.findViewById(R.id.closeButton);

        Glide.with(this).load(offer.getImageURL()).into(dialogImageView);
        dialogTitleTextView.setText(offer.getTitle());
        dialogSubtitleTextView.setText(offer.getDescription());
        dialogCategoryTextView.setText(offer.getCategory());

        MapView dialogMapView = dialog.findViewById(R.id.dialogMapView);
        dialogMapView.onCreate(dialog.onSaveInstanceState());
        dialogMapView.onResume(); // Needed to get the map to display immediately

        dialogMapView.getMapAsync(googleMap -> {
            LatLng offerLocation = new LatLng(offer.getLatitude(), offer.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(offerLocation).title("Offer Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(offerLocation, 15));
        });

        Button navigateButton = dialog.findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + offer.getLatitude() + "," + offer.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        // Close button click listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
