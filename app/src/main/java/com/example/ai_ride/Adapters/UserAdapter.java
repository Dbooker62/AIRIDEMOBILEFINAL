package com.example.ai_ride.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Models.UserOfferModel;
import com.example.ai_ride.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<UserOfferModel> userOfferList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserOfferModel offer);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public UserAdapter(Context context, List<UserOfferModel> userOfferList) {
        this.context = context;
        this.userOfferList = userOfferList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserOfferModel userOffer = userOfferList.get(position);

        holder.titleTextView.setText(userOffer.getTitle());
        holder.categoryTextView.setText(userOffer.getCategory());

        if (userOffer.getDescription() != null) {
            holder.subtitleTextView.setText(userOffer.getDescription());
        } else {
            holder.subtitleTextView.setText("No description available");
        }

        if (userOffer.getImageURL() != null) {
            Glide.with(holder.itemView).load(userOffer.getImageURL()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.add_photo);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(userOffer);
                }
            }
        });

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(userOffer.getLatitude(), userOffer.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                String locationName = addresses.get(0).getAddressLine(0);
                holder.locationTextView.setText(locationName);
            } else {
                holder.locationTextView.setText("Location not available");
            }
        } catch (IOException e) {
            e.printStackTrace();
            holder.locationTextView.setText("Error fetching location");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(userOffer);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userOfferList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView subtitleTextView;
        public TextView categoryTextView;
        public TextView locationTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
    }
}
