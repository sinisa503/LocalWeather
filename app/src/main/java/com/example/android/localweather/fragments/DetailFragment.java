package com.example.android.localweather.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.localweather.R;
import com.example.android.localweather.data.WeatherContract;
import com.example.android.localweather.utility.Utility;

/**
 * Created by SINISA on 11.1.2016..
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private static final int DETAIL_LOADER = 0;
    private Uri mUri;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_LOC_KEY
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mFriendlyDateView,mDescriptionView,mHighTempView,mLowTempView,
            mHumidityView,mWindView,mPressureview, cityNameBig;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get bundle from Main or Detail activity
        Bundle arguments = getArguments();
        if (arguments != null){
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        cityNameBig = (TextView)rootView.findViewById(R.id.city_name_big);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_view_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_description_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.list_item_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.list_item_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.list_item_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.list_item_wind_textview);
        mPressureview = (TextView) rootView.findViewById(R.id.list_item_pressure_textview);
        mIconView = (ImageView) rootView.findViewById(R.id.list_item_icon);

        return rootView;
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    public void onLocationChanged(String newLocation){
        Uri uri = mUri;
        if (uri != null){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation,date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri){
            return new CursorLoader(getActivity(),
                    mUri,DETAIL_COLUMNS,null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        if (data != null && data.moveToFirst()) {
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            mIconView.setImageResource(Utility.getImageFromDrawable(weatherId));

            String cityString = Utility.getPreferredLocation(getActivity()).toUpperCase();
            cityNameBig.setText(cityString);

            //Set text to day of the week and date
            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateTxt = Utility.getFriendlyDayString(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            mFriendlyDateView.setText(friendlyDateTxt);

            //Read description from cursor and update view
            String description = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);

            // Add a content description to the icon field
            mIconView.setContentDescription(description);

            //Read high temperature and update view
            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            mHighTempView.setText(getActivity().getString(R.string.format_temperature, high));

            //Read low temperature and update view
            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            mLowTempView.setText(getActivity().getString(R.string.format_temperature, low));

            //Read humidity and  update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            String humidityTxt = String.valueOf(humidity);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidityTxt));

            //Read wind speed and update view
            float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
            float degrees = data.getFloat(COL_WEATHER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(),windSpeed, degrees ));

            //Read pressure from cursor and update view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureview.setText(getActivity().getString(R.string.format_pressure, pressure));

            //For the share Intent
            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
