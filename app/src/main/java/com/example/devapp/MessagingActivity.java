package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private EditText textMessage;
    private TextView sendMessageButton;
    private RecyclerView recyclerView;
    private List<MessagingModelClass> messagingModelClassesList;
    DatabaseReference messageDatabaseRefReceiver,messageDatabaseRefSender, profileDatabaseReference;

    String receiverUserId;
    String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        getSupportActionBar().hide();

        textMessage = findViewById(R.id.messageTextId);
        sendMessageButton = findViewById(R.id.messageSendBtnId);
        recyclerView = findViewById(R.id.messageRecycleVWId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        receiverUserId = getIntent().getStringExtra("receiveruserId");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();
        messageDatabaseRefReceiver = FirebaseDatabase.getInstance().getReference().child("Message").child(receiverUserId).child(currentUserId);
        messageDatabaseRefSender = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId).child(receiverUserId);

        messagingModelClassesList = new ArrayList<>();

        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Userprofile");

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToReceiver();

            }
        });



        messageDatabaseRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datasnap : snapshot.getChildren()) {
                    MessagingModelClass messagingModelClass = datasnap.getValue(MessagingModelClass.class);
                    messagingModelClassesList.add(messagingModelClass);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


        FirebaseRecyclerOptions<MessagingModelClass> options =
                new FirebaseRecyclerOptions.Builder<MessagingModelClass>()
                        .setQuery(messageDatabaseRefReceiver, MessagingModelClass.class)
                        .build();


        FirebaseRecyclerAdapter<MessagingModelClass, MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MessagingModelClass, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessagingModelClass model) {
                MessagingModelClass messagingModelClass = messagingModelClassesList.get(position);

                holder.messageText.setText(messagingModelClass.getContent());
                if(messagingModelClass.getSenderId().toString().equals(currentUserId)){
                    holder.messageLayout.setPadding(620,0,0,0);
                    int newColor = Color.rgb(70,130,180);
                    holder.messageText.setTextColor(newColor);
                }
                else{
                    holder.messageLayout.setPadding(0,0,50,0);
                }


            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
                return new MessageViewHolder(view);

            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);


    }

    private void sendMessageToReceiver() {
        String messageContent = textMessage.getText().toString().trim();
            String messageId = messageDatabaseRefReceiver.push().getKey();

            if(!messageContent.isEmpty()){
                MessagingModelClass message = new MessagingModelClass(messageId, currentUserId, receiverUserId, messageContent, System.currentTimeMillis());
                messageDatabaseRefReceiver.child(messageId).setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendMessageToSender();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessagingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

    }


    private void sendMessageToSender() {
        String messageContent = textMessage.getText().toString().trim();
        String messageId = messageDatabaseRefSender.push().getKey();

        if(!messageContent.isEmpty()){
            MessagingModelClass message = new MessagingModelClass(messageId, currentUserId, receiverUserId, messageContent, System.currentTimeMillis());
            messageDatabaseRefSender.child(messageId).setValue(message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            textMessage.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MessagingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        TextView receiverUserName = findViewById(R.id.messageReceiverId);
        ImageView receiverUserImage = findViewById(R.id.messageReceiverImage);

        profileDatabaseReference.child((receiverUserId)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String imageUri = snapshot.child("uimage").getValue().toString();
                    Glide.with(getApplicationContext()).load(imageUri).into(receiverUserImage);
                    receiverUserName.setText(snapshot.child("uname").getValue().toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}