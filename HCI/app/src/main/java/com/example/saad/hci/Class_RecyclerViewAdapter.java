package com.example.saad.hci;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Class_RecyclerViewAdapter extends RecyclerView.Adapter<Class_RecyclerViewAdapter.ViewHolder> {
    ArrayList<Class_Trail> all_Trails;
    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView    imagebox_image;
        public TextView     imagebox_text;
        public FrameLayout  imagebox;

        private ViewHolder(View v) {
            super(v);
            this.imagebox_image  = (ImageView) v.findViewById(R.id.imagebox_image);
            this.imagebox_text   = (TextView) v.findViewById(R.id.imagebox_text);
            this.imagebox        = (FrameLayout) v.findViewById(R.id.imagebox);
        }
    }

    public Class_RecyclerViewAdapter(ArrayList<Class_Trail> _all_Trails, Context _mContext) {
        all_Trails   = _all_Trails;
        mContext     = _mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_box, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Class_Trail trail   = (Class_Trail) all_Trails.get(i);
        int image_id        = mContext.getResources().getIdentifier(trail.getFirstImageSrc(), "drawable", mContext.getPackageName());

        viewHolder.imagebox_image.setImageResource(image_id);
        viewHolder.imagebox_text.setText(trail.getName());

        if (i == all_Trails.size()-1) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)(viewHolder.imagebox.getLayoutParams());
            params.setMargins(0, 0, 0, 0);
            viewHolder.imagebox.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)(viewHolder.imagebox.getLayoutParams());
            params.setMargins(0, 0, 4, 0);
            viewHolder.imagebox.setLayoutParams(params);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return all_Trails.size();
    }

    public void updateData(ArrayList<Class_Trail> _all_Trails) {
        all_Trails = _all_Trails;
        notifyDataSetChanged();
    }
}
