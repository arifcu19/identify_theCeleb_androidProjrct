package com.example.devapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Cricketers extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextCountry;
    private TextView loadData;



    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference crickRef = db.collection("Cricketers");

    private DocumentSnapshot lastResult;

    private Button loadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricketers);

        editTextName = findViewById(R.id.edt_Cname);
        editTextCountry = findViewById(R.id.edt_Ccountry);
        loadData = findViewById(R.id.loadCricketers);

        loadButton = findViewById(R.id.loadCricId);

    }


    public void addPlayer(View v) {

        String pName = editTextName.getText().toString();
        String cName = editTextCountry.getText().toString();

        if(pName.isEmpty()){
            editTextName.setError("name required");
            editTextName.requestFocus();
            return;
        }

        else if(cName.isEmpty()){
            editTextCountry.setError("country name required");
            editTextCountry.requestFocus();
            return;
        }

        else{
            CricModelClass cricModelClass = new CricModelClass(pName, cName);
            crickRef.add(cricModelClass);

            editTextName.setText("");
            editTextCountry.setText("");
        }

    }

    public void loadPlayers(View v) {

        loadButton.setText("Load More");

        Query query;
        if (lastResult == null) {
            query = crickRef
                    .limit(4);
        } else {
            query = crickRef
                    .startAfter(lastResult)
                    .limit(4);
        }

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data = "";

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CricModelClass cricModelClass = documentSnapshot.toObject(CricModelClass.class);
                            cricModelClass.setDocumentId(documentSnapshot.getId());

                            String documentId = cricModelClass.getDocumentId();
                            String name = cricModelClass.getName();
                            String country = cricModelClass.getCountry();
                            // int priority = note.getPriority();

                            data += "Name: " + name + "\nCountry: " + country
                                    +"\n\n";
                        }

                        if (queryDocumentSnapshots.size() > 0) {
                            data += "\n";
                            loadData.append(data);

                            lastResult = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);
                        }
                    }
                });
    }
    }
