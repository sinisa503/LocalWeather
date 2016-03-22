package com.example.android.localweather.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.example.android.localweather.R;
import com.example.android.localweather.sync.LocalWeatherSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SINISA on 28.11.2015..
 */
public class Utility {

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }
        return context.getString(R.string.format_temperature, temperature);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        if (julianDay == currentJulianDay) {
            String today = getDayName(context, dateInMillis);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId, today, getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            return getDayName(context, dateInMillis);
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateInMillis) {
//        Time time = new Time();
//        time.setToNow();
//        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat montDayFormat = new SimpleDateFormat("MMM dd");
        String monthDayString = montDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getDayName(Context context, long dateInMillis) {
        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

//        if (julianDay == currentJulianDay) {
//            return context.getString(R.string.today);
//        } else if (julianDay == currentJulianDay + 1) {
//            return context.getString(R.string.tomorow);
//        } else {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        String dateString = dayFormat.format(dateInMillis);
        return buildFirstLetterUppercase(dateString);
        //}
    }

    public static int getImageFromDrawable(int weatherId) {
        // Based on weather code com.example.android.sunshine.data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_stock_weather_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_stock_weather_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_sun;
        } else if (weatherId == 801) {
            return R.drawable.ic_cloud_sun;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloud;
        }
        return -1;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public
    @LocalWeatherSyncAdapter.LocationStatus
    int getLocationStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getString(R.string.pref_location_status_key),
                LocalWeatherSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    public static void resetLocationStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(c.getString(R.string.pref_location_status_key), LocalWeatherSyncAdapter.LOCATION_STATUS_UNKNOWN);
        editor.apply();
    }

    public static String buildFirstLetterUppercase(String word) {
        char[] array = word.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            char c;
            if (i == 0) {
                c = array[i];
                builder.append(String.valueOf(c).toUpperCase());
            } else {
                c = array[i];
                builder.append(String.valueOf(c).toLowerCase());
            }
        }
        return builder.toString();
    }
//    public static boolean isLocationLatLonAvailable(Context context) {
//        SharedPreferences prefs
//                = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.contains(context.getString(R.string.pref_location_latitude))
//                && prefs.contains(context.getString(R.string.pref_location_longitude));
//    }
}
