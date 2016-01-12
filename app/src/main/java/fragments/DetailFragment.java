package fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.R;
import com.example.android.sunshine.Utility;

import data.WeatherContract;

/**
 * Created by SINISA on 11.1.2016..
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private static final int DETAIL_LOADER = 0;
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

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;
    private ImageView mIconView;
    private TextView mFriendlyDateView,mDateView,mDescriptionView,mHighTempView,mLowTempView,
            mHumidityView,mWindView,mPressureview;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_view_day_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_view_date_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_view_description);
        mHighTempView = (TextView) rootView.findViewById(R.id.list_item_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.list_item_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.list_item_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.list_item_wind_textview);
        mPressureview = (TextView) rootView.findViewById(R.id.list_item_pressure_textview);
        mIconView = (ImageView) rootView.findViewById(R.id.list_item_icon);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider == null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d("Sinisa", "Share action provider is null");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        boolean isMetric = Utility.isMetric(getActivity());
        String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String highTemp = Utility.formatTemperature(getActivity().getBaseContext(),data.getDouble(COL_WEATHER_MAX_TEMP),isMetric);
        String lowTemp = Utility.formatTemperature(getActivity().getBaseContext(),data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);


        String friendlyDay = Utility.getFriendlyDayString(getActivity().getBaseContext(),data.getLong(COL_WEATHER_DATE));
        String description = data.getString(COL_WEATHER_DESC);
        String humidity = data.getString(COL_WEATHER_HUMIDITY);
        String wind = data.getString(COL_WEATHER_WIND_SPEED);
        String pressure = data.getString(COL_WEATHER_PRESSURE);

        mFriendlyDateView.setText(friendlyDay);
        mDateView.setText(date);
        mDescriptionView.setText(description);
        mHighTempView.setText(highTemp);
        mLowTempView.setText(lowTemp);
        mHumidityView.setText(humidity);
        mWindView.setText(wind);
        mPressureview.setText(pressure);
        mIconView.setImageResource(Utility.getImageFromDrawable(weatherId));

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
