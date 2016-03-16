package com.example.android.localweather.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.localweather.R;
import com.example.android.localweather.data.WeatherContract;
import com.example.android.localweather.sync.LocalWeatherSyncAdapter;
import com.example.android.localweather.utility.ForecastAdapter;
import com.example.android.localweather.utility.Utility;

//Fetching the forecast and displaying it as a listView
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private boolean mUseTodayLayout;
    private final static int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    // These indices are tied to FORECAST_COLUMNS
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    private ForecastAdapter mForecastAdapter;
    private ListView mListView;
    private Uri mUri;
    private int mListPosition = ListView.INVALID_POSITION;
    private final static String LIST_POSITION_TAG = "selected_position";

    //Sending notification about the item click to the Main activity
    //Main activity will notify Detail Fragment or Detail Activity through method onItemSelected
    public interface Callback {
        void onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);

        //In case the view is empty there is a custom empty list
        View emptyView = rootView.findViewById(R.id.list_view_forecast_empty);
        mListView.setEmptyView(emptyView);

        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                }
                mListPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_POSITION_TAG)) {
            mListPosition = savedInstanceState.getInt(LIST_POSITION_TAG);
        }
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListPosition != ListView.INVALID_POSITION) {
            outState.putInt(LIST_POSITION_TAG, mListPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                openPreferredLocation();
                return true;
//            case R.id.action_refresh:
//                updateWeater();
//                return true;
//            case R.id.settings:
//                Intent i = new Intent(getActivity(), SettingsActivity.class);
//                startActivity(i);
//                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLocationChanged() {
        updateWeater();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeater() {
/*        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(getActivity(),0,alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+3000, pendingIntent);*/
        LocalWeatherSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocation() {

        if (null != mForecastAdapter) {
            Cursor cursor = mForecastAdapter.getCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                String positionLatitude = cursor.getString(COL_COORD_LAT);
                String positionLongitude = cursor.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + positionLatitude + "," + positionLongitude);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Couldn't call: "
                            + geoLocation.toString() + ", no reciveing apps instaled", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Couldn't call: " + geoLocation.toString() + ", no reciveing apps instaled");
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorloader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mListPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mListPosition);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    private void updateEmptyView() {
        if (mForecastAdapter.getCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.list_view_forecast_empty);
            if (null != tv) {
                int messagge = R.string.empty_forecast_list;
                if (!Utility.isNetworkConnected(getActivity())) {
                    messagge = R.string.no_network_info;
                }
                tv.setText(messagge);
            }
        }
    }
}