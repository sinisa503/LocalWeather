package fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.DetailActivity;
import com.example.android.sunshine.JSonParser;
import com.example.android.sunshine.R;
import com.example.android.sunshine.SettingsActivity;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ForecastFragment extends Fragment {

    ArrayAdapter<String> arrayAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        arrayAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_foracast,
                R.id.list_item_forecast_textview, new ArrayList<String>());

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = arrayAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("forecast",forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
         public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                return false;
            case R.id.action_refresh:
                updateWeather();
                return true;
            case R.id.settings:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return false;
    }
    private void updateWeather(){
        FetchWeatherTask run = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cityCode = sharedPreferences.getString(getString
                (R.string.pref_location_key), getString(R.string.pref_location_default));
        run.execute(cityCode);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        String[] result = null;

        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String forecastJSonStr;

            String format = "json";
            String units = "metric";
            int numDays = 7;
            String openWeatherSecKey = "af77c18f3db27fe21fee88821770c17a";

            try {
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?id=3186886&appid=af77c18f3db27fe21fee88821770c17a");

                final String FORACAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORACAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, openWeatherSecKey)
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }
                if (stringBuffer.length() == 0) {
                    return null;
                }
                forecastJSonStr = stringBuffer.toString();
                try {
                    JSonParser jSonParser = new JSonParser(getActivity());
                    result = jSonParser.getWeatherDataFromJson(forecastJSonStr,7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (MalformedURLException e) {
                Log.e("Sinisa", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Sinisa", e.getMessage());
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("Sinisa", e.getMessage());
                        return null;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                arrayAdapter.clear();
                for (String i : result) {
                    arrayAdapter.add(i);
                }
            }
        }
    }
}