package com.example.devapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SoccerCustomAdapter extends RecyclerView.Adapter<SoccerCustomAdapter.customViewHold>{

    public SoccerCustomAdapter(Context mCtx, List<SoccerModelClass> playerList) {
        this.mCtx = mCtx;
        this.playerList = playerList;
    }

    private Context mCtx;
    private List<SoccerModelClass> playerList;

    @NonNull
    @Override
    public customViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View view = layoutInflater.inflate(R.layout.custom_recycle_soccer,null);

        return new customViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull customViewHold holder, int position) {

        SoccerModelClass soccerModelClass = playerList.get(position);

        holder.sPlayerName.setText(soccerModelClass.getPlayerName());
        holder.sPlayerCountry.setText(soccerModelClass.getPlayerCounrty());

    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }


    class customViewHold extends RecyclerView.ViewHolder {

        TextView sPlayerName, sPlayerCountry;
        public customViewHold(@NonNull View itemView) {
            super(itemView);


            sPlayerName = itemView.findViewById(R.id.s_player_name);
            sPlayerCountry = itemView.findViewById(R.id.s_player_country);

            itemView.setTag(itemView);

        }
    }

}
