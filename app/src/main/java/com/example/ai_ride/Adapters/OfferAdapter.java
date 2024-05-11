package com.example.ai_ride.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_ride.Models.AddOfferModel;
import com.example.ai_ride.R;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
    private List<AddOfferModel> offerList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onUpdateClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OfferAdapter(List<AddOfferModel> offerList) {
        this.offerList = offerList;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bussiness_card, parent, false);
        return new OfferViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        AddOfferModel offer = offerList.get(position);
        holder.titleTextView.setText(offer.getTitle());
        holder.subtitleTextView.setText(offer.getDescription());
        holder.categoryTextView.setText(offer.getCategory());
        Glide.with(holder.itemView).load(offer.getImageURL()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, subtitleTextView, categoryTextView;
        ImageView imageView;
        Button updateButton, deleteButton;

        public OfferViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            imageView = itemView.findViewById(R.id.imageView);
            updateButton = itemView.findViewById(R.id.updateButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onUpdateClick(position);
                        }
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
