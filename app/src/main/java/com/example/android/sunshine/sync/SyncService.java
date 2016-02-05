package com.example.android.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by SINISA on 22.1.2016..
 */
public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static LocalWeatherSyncAdapter localWeatherSyncAdapter = null;


    @Override
    public void onCreate() {
        Log.d("SyncService", "onCreate - SyncService");
        synchronized (sSyncAdapterLock) {
            if (localWeatherSyncAdapter == null) {
                localWeatherSyncAdapter = new LocalWeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localWeatherSyncAdapter.getSyncAdapterBinder();
    }
}
