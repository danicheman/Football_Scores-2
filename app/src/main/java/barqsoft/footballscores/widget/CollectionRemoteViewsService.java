package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by NICK on 10/22/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionRemoteViewsService extends RemoteViewsService {

    static final String LOG_TAG = CollectionRemoteViewsService.class.getSimpleName();

    //default row order from table
    static final int INDEX_ID = 0;
    static final int DATE_COL = 1;
    static final int TIME_COL = 2;
    static final int HOME_COL = 3;
    static final int AWAY_COL = 4;
    static final int LEAGUE_COL = 5;
    static final int HOME_GOALS_COL = 6;
    static final int AWAY_GOALS_COL = 7;
    static final int MATCH_ID = 8;
    static final int MATCH_DAY = 9;

    // not putting all columns here because we'll get all from the table



    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mData = null;

            @Override
            public void onCreate() {
                //nada
            }


            @Override
            public void onDataSetChanged() {
                if (mData != null) {
                    mData.close();
                }

                //get the last time the widget was updated.
                final long identityToken = Binder.clearCallingIdentity();
                String lastUpdated = Utilies.getLastUpdated(CollectionRemoteViewsService.this);
                Uri scoresForTodayUri = DatabaseContract.ScoresEntry.buildScoreWithDateToday();

                mData = getContentResolver().query(scoresForTodayUri, null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mData != null) {
                    mData.close();
                    mData = null;
                }
            }

            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mData == null || !mData.moveToPosition(position)) {
                    Log.d(LOG_TAG, "getViewAt returning null; invalid position, empty data, or empty data at position " + position);
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                //todo: Continue Here!
                //build the view for the collection widget
                //int

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {

                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {

                if (mData.moveToPosition(position)) {
                    return mData.getLong(INDEX_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
