package com.example.ai_ride.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Adapters.UserAdapter;
import com.example.ai_ride.Models.UserOfferModel;
import com.example.ai_ride.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeeAllOffers extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserOfferModel> offerList;

    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_offers);
        back = findViewById(R.id.seeAll_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.user_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        offerList = new ArrayList<>();
        adapter = new UserAdapter(this, offerList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserOfferModel offer) {
                showOfferDetailsDialog(offer);
            }
        });

        fetchOffers();

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
                        if (offer != null) {
                            offerList.add(offer);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SeeAllOffers.this, "Failed to fetch offers", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showOfferDetailsDialog(UserOfferModel offer) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.offer_dialog);

        ImageView dialogImageView = dialog.findViewById(R.id.dialogImageView);
        TextView dialogTitleTextView = dialog.findViewById(R.id.dialogTitleTextView);
        TextView dialogSubtitleTextView = dialog.findViewById(R.id.dialogSubtitleTextView);
        TextView dialogCategoryTextView = dialog.findViewById(R.id.dialogCategoryTextView);
        TextView dialogLocationTextView = dialog.findViewById(R.id.dialogLocationTextView);

        Button closeButton = dialog.findViewById(R.id.closeButton);

        Glide.with(this).load(offer.getImageURL()).into(dialogImageView);
        dialogTitleTextView.setText(offer.getTitle());
        dialogSubtitleTextView.setText(offer.getDescription());
        dialogCategoryTextView.setText(offer.getCategory());

        // Get location details using Geocoder

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(offer.getLatitude(), offer.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String addressLine = addresses.get(0).getAddressLine(0);
                dialogLocationTextView.setText(addressLine);
            } else {
                dialogLocationTextView.setText("Location not available");
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting address", e);
            dialogLocationTextView.setText("Location not available");
        }

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