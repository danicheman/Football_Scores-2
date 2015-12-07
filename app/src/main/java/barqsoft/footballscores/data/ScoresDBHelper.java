package barqsoft.footballscores.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import barqsoft.footballscores.data.DatabaseContract.ScoresEntry;
import barqsoft.footballscores.data.DatabaseContract.TeamsEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = ScoresDBHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 5;

    private static final String SQL_CREATE_SCORES_TABLE = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
            + ScoresEntry._ID + " INTEGER PRIMARY KEY,"
            + ScoresEntry.DATE_COL + " TEXT NOT NULL,"
            + ScoresEntry.TIME_COL + " INTEGER NOT NULL,"
            + ScoresEntry.HOME_COL + " TEXT NOT NULL,"
            + ScoresEntry.HOME_ID_COL + " INTEGER NOT NULL,"
            + ScoresEntry.AWAY_COL + " TEXT NOT NULL,"
            + ScoresEntry.AWAY_ID_COL + " INTEGER NOT NULL,"
            + ScoresEntry.LEAGUE_COL + " INTEGER NOT NULL,"
            + ScoresEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
            + ScoresEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
            + ScoresEntry.MATCH_ID + " INTEGER NOT NULL,"
            + ScoresEntry.MATCH_DAY + " INTEGER NOT NULL,"
            + " UNIQUE (" + ScoresEntry.MATCH_ID + ") ON CONFLICT REPLACE"
            + " );";

    private static final String SQL_CREATE_TEAMS_TABLE = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
            + TeamsEntry._ID + " INTEGER PRIMARY KEY, "
            + TeamsEntry.NAME_COL + " TEXT NOT NULL, "
            + TeamsEntry.SHORT_NAME_COL + " TEXT NOT NULL, "
            + TeamsEntry.CREST_URL_COL + " TEXT NOT NULL, "
            + " UNIQUE ( " + TeamsEntry.NAME_COL + ") ON CONFLICT REPLACE"
            + ");";

    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try {
            db.execSQL(SQL_CREATE_SCORES_TABLE);
            db.execSQL(SQL_CREATE_TEAMS_TABLE);
            Log.d(LOG_TAG, SQL_CREATE_TEAMS_TABLE);
        } catch (SQLException se) {

            Log.e(LOG_TAG, "onCreate Databases: "+se.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
        onCreate(db);
    }
}
