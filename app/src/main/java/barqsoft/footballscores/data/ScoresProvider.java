package barqsoft.footballscores.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider
{
    private static final String LOG_TAG = ScoresProvider.class.getSimpleName();

    private ScoresDBHelper mOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int MATCHES_WITH_DAY = 104;
    private static final int TEAMS = 200;
    private static final int TEAMS_WITH_ID = 201;
    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.ScoresEntry.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =
            DatabaseContract.ScoresEntry.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =
            DatabaseContract.ScoresEntry.MATCH_ID + " = ?";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "scores" , MATCHES);
        matcher.addURI(authority, "league" , MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, "id" , MATCHES_WITH_ID);
        matcher.addURI(authority, "date" , MATCHES_WITH_DATE);
        matcher.addURI(authority, "date/#", MATCHES_WITH_DAY);
        matcher.addURI(authority, "teams", TEAMS);
        matcher.addURI(authority, "teams/#", TEAMS_WITH_ID);
        return matcher;
    }

    /*private int match_uri(Uri uri)
    {
        String link = uri.toString();
        {
           if(link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString()))
           {
               return MATCHES;
           } else if (link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithDate().toString()))
           {
               return MATCHES_WITH_DATE;
           } else if (link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithId().toString()))
           {
               return MATCHES_WITH_ID;
           } else if (link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithLeague().toString()))
           {
               return MATCHES_WITH_LEAGUE;
           } else if (link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithDateToday().toString())) {
               return MATCHES_WITH_DAY;
           } else if (link.contentEquals(DatabaseContract.TeamsEntry.buildTeamUri().toString())) {
               return TEAMS;
           }
        }
        return -1;
    }*/
    @Override
    public boolean onCreate()
    {
        Log.d(LOG_TAG, "Scores provider on create, should launch the database creation.");
        mOpenHelper = new ScoresDBHelper(getContext());
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.ScoresEntry.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DAY:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case TEAMS:
                return DatabaseContract.TeamsEntry.CONTENT_ITEM_TYPE;
            case TEAMS_WITH_ID:
                return DatabaseContract.TeamsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown type uri :" + uri );
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        //int match = match_uri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (muriMatcher.match(uri))
        {
            case MATCHES: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection,null,null,null,null,sortOrder); break;
            case MATCHES_WITH_DATE:

                String JOIN_QUERY = "SELECT scoresEntry.*, home.crest_url as home_crest, away.crest_url as away_crest  " +
                        "               FROM scoresEntry "+
                                        "LEFT JOIN teamsEntry AS home ON home._id = scoresEntry.home_id " +
                                        "LEFT JOIN teamsEntry AS away ON away._id = scoresEntry.away_id " +
                                        "WHERE date = \""+selectionArgs[0]+"\"";


                retCursor = mOpenHelper.getReadableDatabase().rawQuery(JOIN_QUERY, null);

                Log.d(LOG_TAG, "first selection args = "+ selectionArgs[0]+ " Result rows: "+ retCursor.getCount());
                /*retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);*/
                break;
            case MATCHES_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.SCORES_TABLE,
                    projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_DAY:
                Log.v(LOG_TAG, "Matches with day matched in select, getting 5 latest matches");
                //get date value out of uri
                /*String date = uri.getLastPathSegment();
                String[] dateArray = {date};

                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, dateArray, null, null, sortOrder);*/
                retCursor = mOpenHelper.getReadableDatabase().query(DatabaseContract.SCORES_TABLE,null,null,null,null,null,"_id desc", "10");
                break;
            case TEAMS:
                retCursor = mOpenHelper.getReadableDatabase().query(DatabaseContract.TEAMS_TABLE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Query Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (muriMatcher.match(uri)){
            case TEAMS:
            //case TEAMS_WITH_ID:
                long _id = db.insertWithOnConflict(DatabaseContract.TEAMS_TABLE,null, values, db.CONFLICT_IGNORE);
                if( _id > 0) {
                    Log.d(LOG_TAG, "Team inserted with id: "+_id);
                    return DatabaseContract.TeamsEntry.buildTeamUriWithId(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into "+ DatabaseContract.TEAMS_TABLE);
                }
            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));

        switch (muriMatcher.match(uri))
        {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try
                {
                    Log.d(LOG_TAG, "BulkInsert - Matches: trying to insert this many rows" + values.length);
                    for(ContentValues value : values)
                    {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                        {
                            returncount++;
                        }
                    }
                    Log.d(LOG_TAG, "Inserted "+returncount+ " rows");
                    Log.d(LOG_TAG, "got this many value rows:"+values.length);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returncount;
            case TEAMS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    Log.d(LOG_TAG, "inserting "+values.length + " teams");
                    for (ContentValues value: values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.TEAMS_TABLE, null,
                                value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1L) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return returnCount;
            default:
                Log.d(LOG_TAG, "did not match in bulk insert :(");
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
