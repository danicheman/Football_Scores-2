package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    public static final String SCORES_TABLE = "ScoresEntry";
    public static final String TEAMS_TABLE = "TeamsEntry";

    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores.service";


    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final class ScoresEntry implements BaseColumns
    {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_ID_COL = "home_id";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String AWAY_ID_COL = "away_id";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";


        public static final String PATH = "scores";

        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildScoreWithLeague()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }

        public static Uri buildScoreWithDateToday() {
            long unixTime = System.currentTimeMillis() / 1000L;
            return BASE_CONTENT_URI.buildUpon().appendPath("date").appendPath(Long.toString(unixTime)).build();
        }
    }
    public static final class TeamsEntry implements BaseColumns {

        public static final String PATH = "teams";
        public static final String NAME_COL = "name";
        public static final String SHORT_NAME_COL = "short_name";
        public static final String CREST_URL_COL = "crest_url";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildTeamUriWithId(long id) {

            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildTeamUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        }
    }

}
