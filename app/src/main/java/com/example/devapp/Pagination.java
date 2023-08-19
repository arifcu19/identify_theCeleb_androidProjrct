package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Pagination extends AppCompatActivity {

    private TextView nextButton, previousButton,  pageNumberText, previousPageNumberText;

    private RecyclerView recyclerView;
    private PaginationAdapter adapter;
    private DatabaseReference databaseRef;
    private List<UserHistoryDetails> dataList;
    private int currentPage = 0;
    private int pageSize = 6;
    private ProgressBar progressBar;
    String userId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagination);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference().child("CelebList");

        recyclerView = findViewById(R.id.recyclePage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        adapter = new PaginationAdapter(dataList);
        recyclerView.setAdapter(adapter);
        progressBar = findViewById(R.id.progressPaginationId);

        pageNumberText = findViewById(R.id.page_number_text);
        previousPageNumberText = findViewById(R.id.previous_page_number_text);


        loadNextPage();
        progressBar.setVisibility(View.INVISIBLE);

        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loadNextPage();
            }
        });

        previousButton = findViewById(R.id.previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loadPreviousPage();
            }
        });
    }


    private void loadNextPage() {
        Query query = databaseRef.orderByKey().limitToFirst((currentPage + 1) * pageSize);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataList.clear();
                    int count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (count >= currentPage * pageSize) {
                            UserHistoryDetails data = snapshot.getValue(UserHistoryDetails.class);
                            dataList.add(data);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        count++;
                    }
                    adapter.notifyDataSetChanged();
                    currentPage++;
                    updatePageNumber();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void loadPreviousPage() {
        if (currentPage > 0) {
            progressBar.setVisibility(View.VISIBLE);
            currentPage--;
            Query query = databaseRef.orderByKey().endBefore(String.valueOf(currentPage * pageSize)).limitToLast(pageSize);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    if (dataSnapshot.exists()) {
                        Toast.makeText(Pagination.this, "loading", Toast.LENGTH_SHORT).show();
                        List<UserHistoryDetails> tempList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserHistoryDetails data = snapshot.getValue(UserHistoryDetails.class);
                            tempList.add(data);
                        }
                        // Clear the dataList and then add data from the tempList
                        dataList.clear();
                        dataList.addAll(tempList);
                        adapter.notifyDataSetChanged();
                        updatePageNumber();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    private void updatePageNumber() {

        int displayPreviousPageNumber = currentPage;
        previousPageNumberText.setText(""+displayPreviousPageNumber);

        int displayPageNumber = currentPage + 1;
        pageNumberText.setText("  "+displayPageNumber);

    }
}