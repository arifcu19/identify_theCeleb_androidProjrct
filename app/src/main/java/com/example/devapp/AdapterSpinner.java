package com.example.devapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterSpinner extends BaseAdapter {

    Context context;
    int [] images;
    String [] fields;


    public AdapterSpinner(Context context, int[] images, String[] fields) {
        this.context = context;
        this.images = images;
        this.fields = fields;
    }

    @Override
    public int getCount() {
        return fields.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view =  layoutInflater.inflate(R.layout.sample_spinner,null,false);
        }
        ImageView imageView = view.findViewById(R.id.imageSpinner);
        imageView.setImageResource(images[i]);

        TextView nameText = view.findViewById(R.id.fieldsName);
        nameText.setText(fields[i]);

        return view;
    }
}
