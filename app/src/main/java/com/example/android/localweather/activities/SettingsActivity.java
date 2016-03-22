package com.example.android.localweather.activities;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.example.android.localweather.R;
import com.example.android.localweather.data.WeatherContract;
import com.example.android.localweather.sync.LocalWeatherSyncAdapter;
import com.example.android.localweather.utility.Utility;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = PreferenceActivity.class.getSimpleName();
    public final static int PLACE_PICKER_REQUEST = 9090;
    private ImageView mAttribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        Preference finishPref = findPreference(getString(R.string.finish_pref_key));
        finishPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                finish();
                return true;
            }
        });

        bindPreferenceSummary(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummary(findPreference(getString(R.string.pref_units_key)));

        // If we are using a PlacePicker location, we need to show attributions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mAttribution = new ImageView(this);
            mAttribution.setImageResource(R.drawable.powered_by_google_light);

//            if (!Utility.isLocationLatLonAvailable(this)) {
//                mAttribution.setVisibility(View.GONE);
//            }

            //setListFooter(mAttribution);
        }
    }

    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void bindPreferenceSummary(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        setPreferenceSummary(preference, PreferenceManager.getDefaultSharedPreferences(
                preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setPreferenceSummary(preference, newValue);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(this.getString(R.string.pref_location_key))) {
            if (mAttribution != null) {
                mAttribution.setVisibility(View.GONE);
            }

            Utility.resetLocationStatus(this);
            LocalWeatherSyncAdapter.syncImmediately(this);
        } else if (key.equals(this.getString(R.string.pref_units_key))) {
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);

        } else if (key.equals(getString(R.string.pref_location_status_key))) {
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummary(locationPreference);
        }
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (key.equals(getString(R.string.pref_location_key))) {
            @LocalWeatherSyncAdapter.LocationStatus int status = Utility.getLocationStatus(this);
            switch (status) {
                case LocalWeatherSyncAdapter.LOCATION_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;
                case LocalWeatherSyncAdapter.LOCATION_STATUS_INVALID:
                    preference.setSummary(getString(R.string.pref_location_error_description));
                    break;
                case LocalWeatherSyncAdapter.LOCATION_STATUS_UNKNOWN:
                    preference.setSummary(getString(R.string.pref_location_unknown_description));
                    break;
                default:
                    preference.setSummary(stringValue);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String city_name = place.getName().toString();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.pref_location_key), city_name);
                editor.commit();

                Preference preference = findPreference(getString(R.string.pref_location_key));
                setPreferenceSummary(preference, city_name);

                // Add attributions for our new PlacePicker location.
                if (mAttribution != null) {
                    mAttribution.setVisibility(View.VISIBLE);
                } else {
                    // For pre-Honeycomb devices, cannot add a footer, so use a snackbar
                    View rootView = findViewById(android.R.id.content);
                    Snackbar.make(rootView, getString(R.string.attribution_text),
                            Snackbar.LENGTH_LONG).show();
                }

                Utility.resetLocationStatus(this);
                LocalWeatherSyncAdapter.syncImmediately(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }


     //A place has been received; use requestCode to track the request.
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlaceAutocomplete.getPlace(this, data);
//                Log.i(TAG, "Place: " + place.getName());
//            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
//                Status status = PlaceAutocomplete.getStatus(this, data);
//                // TODO: Handle the error.
//                Log.i(TAG, status.getStatusMessage());
//
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//        }
//    }
}