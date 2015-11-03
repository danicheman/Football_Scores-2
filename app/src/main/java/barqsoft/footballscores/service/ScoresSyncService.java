package barqsoft.footballscores.service;

/**
 * Created by NICK on 10/30/2015.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ScoresSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ScoresSyncAdapter sScoresSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ScoresSyncService", "onCreate - ScoresSyncService");
        synchronized (sSyncAdapterLock) {
            if (sScoresSyncAdapter == null) {
                sScoresSyncAdapter = new ScoresSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sScoresSyncAdapter.getSyncAdapterBinder();
    }
}