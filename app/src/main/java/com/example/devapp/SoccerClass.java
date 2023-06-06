package com.example.devapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SoccerClass extends AppCompatActivity {


    private static final String JSON_URL = "http://10.0.2.2/android/devapps/getAllPlayer.php";

    RecyclerView recyclerView;
    List<SoccerModelClass> playerList;
    SoccerCustomAdapter soccerCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer_class);

        recyclerView = (RecyclerView) findViewById(R.id.recycleVw_Soccer);
        addData();
    }

    public void addData () {
        playerList = new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject player = array.getJSONObject(i);


                                //adding the player to player list
                                playerList.add(new SoccerModelClass(

                                        player.getInt("playerId"),
                                        player.getString("playerName"),
                                        player.getString("playerCountry")
                                ));

                            }

                            //creating adapter object and setting it to recyclerview
                            soccerCustomAdapter = new SoccerCustomAdapter(getApplicationContext(), playerList);

                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.setAdapter(soccerCustomAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);

    }
}