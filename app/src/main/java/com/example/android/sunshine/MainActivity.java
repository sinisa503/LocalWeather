package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import fragments.DetailFragment;
import fragments.ForecastFragment;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;

            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG).commit();
            }else {
                mTwoPane = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void openPreferredLocation() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }else {
            Log.d("Sinisa", "Couldn't call: " + location + ", no reciveing apps instaled");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if (location != null && location.equals(mLocation)){
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (null != forecastFragment){
                forecastFragment.onLocationChanged();
            }
            DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager().findFragmentByTag(
                    DETAIL_FRAGMENT_TAG);
            if (detailFragment != null){
                detailFragment.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane){
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container,
                    detailFragment,DETAIL_FRAGMENT_TAG).commit();
        }else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            startActivity(intent);
        }
    }
}
