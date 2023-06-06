package com.example.devapp;

import com.google.firebase.firestore.Exclude;

public class CricModelClass {

    private String documentId;
    private String name;
    private String country;

    public CricModelClass() {
    }

    public CricModelClass(String name, String country) {
        this.name = name;
        this.country = country;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

}
