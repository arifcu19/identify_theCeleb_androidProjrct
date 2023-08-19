package com.example.devapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class UserHistoryViewHolder extends RecyclerView.ViewHolder {
    ImageView history_image_View;
    TextView history_TextView;
    CardView bottomSheet;


    public UserHistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        history_image_View = itemView.findViewById(R.id.historyImageView);
        history_TextView = itemView.findViewById(R.id.historyTextView);
        bottomSheet = itemView.findViewById(R.id.userHistoryCard_view);
    }


}

