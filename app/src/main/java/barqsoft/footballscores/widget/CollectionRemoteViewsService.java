package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by NICK on 10/22/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionRemoteViewsService extends RemoteViewsService {

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
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                return null;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }

}
