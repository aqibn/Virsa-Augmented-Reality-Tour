package com.example.saad.hci;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

public class Class_MyTextSliderView extends BaseSliderView {
    public Class_MyTextSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.my_text_slider_view, null);
        ImageView target = (ImageView)v.findViewById(R.id.my_slider_image);
        TextView description = (TextView)v.findViewById(R.id.my_slider_description);
        description.setText(getDescription());
        bindEventAndShow(v, target);
        return v;
    }
}
