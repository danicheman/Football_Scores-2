package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.data.DatabaseContract.ScoresEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + DatabaseContract.ScoresEntry._ID + " INTEGER PRIMARY KEY,"
                + DatabaseContract.ScoresEntry.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.TIME_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.AWAY_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.LEAGUE_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + ScoresEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresEntry.MATCH_ID + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresEntry.MATCH_DAY + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.ScoresEntry.MATCH_ID + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
    }
}
