package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CollectionIntentService extends IntentService {

    private static final String LOG_TAG = CollectionIntentService.class.getSimpleName();

    public CollectionIntentService() {
        super("CollectionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                CollectionProvider.class));

        Uri scoresForTodayUri = DatabaseContract.ScoresEntry.buildScoreWithDateToday();
        Cursor data = getContentResolver().query(scoresForTodayUri, null, null, null, null);

        //check result
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            //todo: select view based on dimension of view
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list);
            Log.e(LOG_TAG, "Collection intent service for appwidgetid: " + appWidgetId);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void getWidgetWidth(AppWidgetManager param1, int param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getWidgetWidthFromOptions(RemoteViews param1, String param2) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
