package com.example.android.sunshine.utility;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.R;

/**
 * Created by SINISA on 10.1.2016..
 */
public class ViewHolder {
    public final ImageView iconView;
    public TextView dateView;
    public TextView descriptionView;
    public TextView highTempView;
    public TextView lowTempView;

    public ViewHolder(View view) {
        iconView = (ImageView)view.findViewById(R.id.list_item_icon);
        dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        highTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
        lowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);
    }
}
