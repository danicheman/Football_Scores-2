package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
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
 * Created by nick on 11/21/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String LOG_TAG = CollectionRemoteViewsFactory.class.getSimpleName();
    private static final String TAG = "CollectionRemoteViewsFactory";
    private Cursor mData = null;
    private Context mContext;

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



    public CollectionRemoteViewsFactory(Context context) {
        //recieve context when class is created
        Log.d(LOG_TAG, "constructor with ( context )");
        mContext = context;
    }

    //default constructor
    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mData != null) {
            mData.close();
        }
        Log.d(LOG_TAG, "onDataSetChanged()");
        //get the last time the widget was updated.
        final long identityToken = Binder.clearCallingIdentity();
        String lastUpdated = Utilies.getLastUpdated(mContext);
        Log.d(TAG, "onDataSetChanged: Last updated date: "+ lastUpdated);
                
        Uri scoresForTodayUri = DatabaseContract.ScoresEntry.buildScoreWithDateToday();
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list);
        views.setTextViewText(R.id.last_updated, "Last Updated: "+ lastUpdated);
        mData = mContext.getContentResolver().query(scoresForTodayUri, null, null, null, null);
        Log.d(LOG_TAG, "Got this many rows from DB: " + mData.getCount());
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
        if(mData == null) {
            Log.d(LOG_TAG, "no data in remote view factory");

        } else {
            Log.d(LOG_TAG, "data in remote view factory has "+ mData.getCount()+ " rows.");
        }
        return mData == null ? 0 : mData.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                mData == null || !mData.moveToPosition(position)) {
            Log.d(LOG_TAG, "getViewAt returning null; invalid position, empty data, or empty data at position " + position);
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_list_item);

        //todo: Continue Here!
        //build the view for the collection widget
        Log.e(LOG_TAG, "getting a part of the view at position " + position);
        Log.e(LOG_TAG, "home team is : " + mData.getString(HOME_COL));

        Uri scoresUri = DatabaseContract.ScoresEntry.buildScoreWithDateToday();
        final Intent fillInIntent = new Intent();
        fillInIntent.setData(scoresUri);
        views.setTextViewText(R.id.team1, mData.getString(HOME_COL));
        views.setTextViewText(R.id.team2, mData.getString(AWAY_COL));
        //requires api 11 and we're supporting back to 10...
        //views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {

        return new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
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
}

