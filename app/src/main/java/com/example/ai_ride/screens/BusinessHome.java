package com.example.ai_ride.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ai_ride.Adapters.OfferAdapter;
import com.example.ai_ride.Models.AddOfferModel;
import com.example.ai_ride.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BusinessHome extends AppCompatActivity implements OfferAdapter.OnItemClickListener {
    private static final String TAG = "BusinessHome";

    Button addOffer;
    RecyclerView recyclerView;
    OfferAdapter adapter;
    List<AddOfferModel> offerList;
    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_home);

        addOffer = findViewById(R.id.add_offer);
        recyclerView = findViewById(R.id.business_recycler);
        offerList = new ArrayList<>();
        adapter = new OfferAdapter(offerList);

        profile = findViewById(R.id.business_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BusinessHome.this, BusinessProfile.class));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BusinessHome.this, AddOffer.class));
            }
        });

        adapter.setOnItemClickListener(this);

        // Fetch data from the database for the current user's offers
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Business").child(userId).child("Offers");
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    offerList.clear();

                    for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                        AddOfferModel offer = offerSnapshot.getValue(AddOfferModel.class);
                        offerList.add(offer);
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching data", databaseError.toException());
                }
            });
        }
    }

    @Override
    public void onUpdateClick(int position) {
        // Handle update action
        AddOfferModel offer = offerList.get(position);
        Intent intent = new Intent(BusinessHome.this, AddOffer.class);
        intent.putExtra("offer", offer);
        intent.putExtra("position", position);
        startActivity(intent);
    }
     @Override
    public void onDeleteClick(int position) {
        // Handle delete action
        if (position >= 0 && position < offerList.size()) {
            AddOfferModel offer = offerList.get(position);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (userId != null) {
                DatabaseReference offersRef = FirebaseDatabase.getInstance().getReference("Business").child(userId).child("Offers");

                 offersRef.orderByChild("title").equalTo(offer.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                             snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error deleting offer from database", databaseError.toException());
                    }
                });

                 offerList.remove(position);
                adapter.notifyItemRemoved(position);
            } else {
                Log.e(TAG, "User ID is null");
            }
        } else {
            Log.e(TAG, "Invalid position");
        }
    }



}
