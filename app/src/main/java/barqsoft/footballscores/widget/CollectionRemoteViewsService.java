package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * Created by NICK on 10/22/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionRemoteViewsService extends RemoteViewsService {

    private static final String TAG = "CollectionRemoteViewsService";
    // not putting all columns here because we'll get all from the table



    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CollectionRemoteViewsFactory((Context)CollectionRemoteViewsService.this);
    }

}


