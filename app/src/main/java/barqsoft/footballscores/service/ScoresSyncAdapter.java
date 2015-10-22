package barqsoft.footballscores.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by NICK on 10/22/2015.
 * <p/>
 * Pull data from api
 */
public class ScoresSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ScoresSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_DATA_UPDATED";

    // Interval at which to sync the football scores, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    public ScoresSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

    }
}
