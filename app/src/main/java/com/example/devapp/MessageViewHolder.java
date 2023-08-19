package com.example.devapp;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageViewHolder extends RecyclerView.ViewHolder{

    TextView messageText;
    LinearLayout messageLayout;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageDisplayId);
        messageLayout = itemView.findViewById(R.id.messageLinearLayout);
    }
}
