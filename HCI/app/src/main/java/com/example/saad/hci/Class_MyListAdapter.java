package com.example.saad.hci;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Class_MyListAdapter extends BaseAdapter {
    Context                         mContext;
    LayoutInflater                  mInflater;
    ArrayList<Class_HeritageSite>   all_HeritageSites;

    public Class_MyListAdapter(Context context, LayoutInflater inflater) {
        mContext            = context;
        mInflater           = inflater;
        all_HeritageSites   = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return all_HeritageSites.size();
    }

    @Override
    public Object getItem(int position) {
        return all_HeritageSites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public ImageView    imagebox_image;
        public ImageView[]  imagebox_popularity;
        public TextView     imagebox_name;
        public TextView     imagebox_address;
        public TextView     imagebox_label;

        public ViewHolder() {
            imagebox_image      = null;
            imagebox_popularity = new ImageView[5];
            imagebox_name       = null;
            imagebox_address    = null;
            imagebox_label      = null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);

            holder = new ViewHolder();
            holder.imagebox_image           = (ImageView) convertView.findViewById(R.id.list_picture);
            holder.imagebox_name            = (TextView) convertView.findViewById(R.id.list_name);
            holder.imagebox_address         = (TextView) convertView.findViewById(R.id.list_address);
            holder.imagebox_label           = (TextView) convertView.findViewById(R.id.list_value);
            holder.imagebox_popularity[0]   = (ImageView) convertView.findViewById(R.id.popularity_icon1);
            holder.imagebox_popularity[1]   = (ImageView) convertView.findViewById(R.id.popularity_icon2);
            holder.imagebox_popularity[2]   = (ImageView) convertView.findViewById(R.id.popularity_icon3);
            holder.imagebox_popularity[3]   = (ImageView) convertView.findViewById(R.id.popularity_icon4);
            holder.imagebox_popularity[4]   = (ImageView) convertView.findViewById(R.id.popularity_icon5);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Class_HeritageSite heritageSite = (Class_HeritageSite) getItem(position);
        int image_id = mContext.getResources().getIdentifier(heritageSite.getImageMain(), "drawable", mContext.getPackageName());

        holder.imagebox_image.setImageResource(image_id);

        holder.imagebox_name.setText(heritageSite.getName());

        holder.imagebox_address.setText("Address: " + heritageSite.getAddress());

        int f = 5;
        while (heritageSite.getPopularity() < f) {
            f--;
            holder.imagebox_popularity[f].setVisibility(View.INVISIBLE);
        }

        float dist = (float)(((int)(heritageSite.calculateDistance(((Class_MyApplication)mContext.getApplicationContext()).curLoc) * 10))/10.0);

        if (dist < 1000) {
            holder.imagebox_label.setText(String.format("%.1f", dist) + " m");
        } else {
            dist = dist / 1000;
            holder.imagebox_label.setText(String.format("%.1f", dist) + " km");
        }

        return convertView;
    }

    public void updateData(ArrayList<Class_HeritageSite> _all_HeritageSites) {
        all_HeritageSites = _all_HeritageSites;
        notifyDataSetChanged();
    }
}
