package com.example.devapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postText, userPostName;
        ImageView userPostImage;

public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        postText = itemView.findViewById(R.id.postDisplayId);
        userPostImage = itemView.findViewById(R.id.userPostImageId);
        userPostName = itemView.findViewById(R.id.userPostNameId);
   }
}
