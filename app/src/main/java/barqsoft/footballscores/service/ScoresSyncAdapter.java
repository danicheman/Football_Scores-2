package barqsoft.footballscores.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilites;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by NICK on 10/22/2015.
 * <p/>
 * Pull data from api
 */
public class ScoresSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = ScoresSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_DATA_UPDATED";

    // Interval at which to sync the football scores, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    //JSON variable name constants
    private static final String LINKS = "_links";
    private static final String SELF = "self";
    private static final String HREF = "href";

    public ScoresSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    //network code goes heeeere
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        //protect the api, don't sync more frequently than 5 minutes at a time.
        long currentTimeMillis = System.currentTimeMillis();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        long lastSyncMinutes = sp.getLong("lastSync", 0);

        getData("n2");
        getData("p2");


        editor.putLong(Utilites.PREF_KEY_LAST_UPDATED, currentTimeMillis);
        editor.commit();

        Log.d(LOG_TAG, "onPerformSync: "+currentTimeMillis);

    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/v1/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days


        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();

        JSONObject matchData = Utilites.getJsonFromUrl(fetch_build,getContext());
        if(matchData == null) {
            Utilites.doToast(getContext(),getContext().getString(R.string.no_response));
//            Log.e(LOG_TAG, "getData: No data retrieved from url!");
            return;
        }

        try {
            //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
            JSONArray matches = matchData.getJSONArray("fixtures");
            if (matches.length() > 0) {
                processJSONdata(matchData, getContext(), true);
            } else {
                //if there is no data, call the function on dummy data
                //this is expected behavior during the off season.
                Log.d(LOG_TAG, "Using dummy data");
                processJSONdata(new JSONObject(getContext().getString(R.string.dummy_data)), getContext(), false);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void processJSONdata(JSONObject jsonResult, Context context, boolean isReal) {
        //todo: get league from utility

        final String MATCH_LINK = "http://api.football-data.org/v1/fixtures/";
        final String FIXTURES = "fixtures";

        final String HOME_TEAM_LINK = "homeTeam";
        final String AWAY_TEAM_LINK = "awayTeam";
        final String SOCCER_SEASON = "soccerseason";

        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String leagueLink = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String homeTeamLink = null;
        String Away = null;
        String awayTeamLink = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        int leagueId;
        int awayTeamId;
        int homeTeamId;
        int soccerSeasonId;
        try {
            JSONArray matches = jsonResult.getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                leagueLink = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                homeTeamLink = match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM_LINK).
                        getString("href");
                awayTeamLink = match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM_LINK).
                        getString("href");


                leagueId = Utilites.getLastUrlSegmentAsInt(leagueLink);
                awayTeamId = Utilites.getLastUrlSegmentAsInt(homeTeamLink);
                checkTeamOrGetLeague(awayTeamId, leagueId, context);

                homeTeamId = Utilites.getLastUrlSegmentAsInt(awayTeamLink);
                //Since it's getting the whole league, this isn't necessary.
                //checkTeamOrGetLeague(homeTeamId, leagueId, context);


                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                String leagueName = Utilites.getLeague(leagueId,context);

                //if the league name is not unknown..
                if (!leagueName.equals(context.getString(R.string.unknown))) {

                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            Log.d(LOG_TAG, "Using dummy data, modifying date");
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.ScoresEntry.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.ScoresEntry.DATE_COL, mDate);
                    match_values.put(DatabaseContract.ScoresEntry.TIME_COL, mTime);
                    match_values.put(DatabaseContract.ScoresEntry.HOME_COL, Home);
                    match_values.put(DatabaseContract.ScoresEntry.HOME_ID_COL, homeTeamId);
                    match_values.put(DatabaseContract.ScoresEntry.AWAY_COL, Away);
                    match_values.put(DatabaseContract.ScoresEntry.AWAY_ID_COL, awayTeamId);
                    match_values.put(DatabaseContract.ScoresEntry.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.ScoresEntry.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.ScoresEntry.LEAGUE_COL, leagueName);
                    match_values.put(DatabaseContract.ScoresEntry.MATCH_DAY, match_day);
                    //log spam

//                    Log.v(LOG_TAG, match_id);
//                    Log.v(LOG_TAG, mDate);
//                    Log.v(LOG_TAG, mTime);
                    Log.v(LOG_TAG, "Home:" + Home);
                    Log.v(LOG_TAG, "Away:" + Away);
//                    Log.v(LOG_TAG, Home_goals);
//                    Log.v(LOG_TAG, Away_goals);

                    values.add(match_values);
                } else {
                    Log.e(LOG_TAG, "!!Unknown League with id: "+ leagueId);
                }
            }

            saveScores(values, context);
            updateWidgets();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    /**
     * Save games/scores retrieved from the api into the database. Return
     * the number of rows inserted.
     *
     * @param scores  Contentvalue Vector
     * @param context
     * @return
     */
    private int saveScores(Vector<ContentValues> scores, Context context) {
        int insertedRowCount = 0;

        ContentValues[] insert_data = new ContentValues[scores.size()];
        scores.toArray(insert_data);
        Log.v(LOG_TAG, "should be adding this many rows: " + scores.size());
        insertedRowCount = context.getContentResolver().bulkInsert(
                DatabaseContract.ScoresEntry.CONTENT_URI, insert_data);
        if(insert_data.length > 0) {
            Log.v(LOG_TAG, "Succesfully Inserted : " + insertedRowCount);

        } else {
            Log.v(LOG_TAG, "No data to insert!!!");
        }

        return insertedRowCount;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.e(LOG_TAG, "getSyncAccount: Error adding account");
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        } else {
            Log.e(LOG_TAG, "getSyncAccount: Account already exists.");
        }
        return newAccount;
    }


    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.i(LOG_TAG, "sync Immediately!");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
//        Log.e(LOG_TAG, "configurePeriodicSync() called with: " + " syncInterval = [" + syncInterval + "], flexTime = [" + flexTime + "]");
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }

    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ScoresSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {


        getSyncAccount(context);
        //Utilites.logSyncs(context);

        AccountManager am = AccountManager.get(context);
        Account account = am.getAccountsByType(context.getString(R.string.sync_account_type))[0];
        boolean isYourAccountSyncEnabled = ContentResolver.getSyncAutomatically(account, context.getString(R.string.content_authority));

        //todo: Remove these from here, do the check when the app is open or when the widget is created
        //if (!isMasterSyncEnabled) Utilites.doToast(context, context.getResources().getString(R.string.no_global_sync));

        if (!isYourAccountSyncEnabled)Utilites.doToast(context,context.getResources().getString(R.string.no_app_sync));
    }

    /**
     * Download league data from API to the Database
     *
     * @param leagueId
     * @param context
     */
    private void getLeagueData(int leagueId, Context context) {

        String leagueLink = "http://api.football-data.org/v1/soccerseasons/"+leagueId+"/teams";
        JSONObject result = Utilites.getJsonFromUrl(Uri.parse(leagueLink),context);

        if(result != null) {

            String dataTeamName = "name";
            String dataShortTeamName = "shortName";
            String dataCrestUrl = "crestUrl";

            //get data to save
            try {
                JSONArray teams = result.getJSONArray("teams");
                Log.i(LOG_TAG, "getTeamData: Saving "+teams.length()+" teams for league "+leagueId);
                Vector<ContentValues> values = new Vector<ContentValues>(teams.length());

                for (int i = 0; i < teams.length(); i++ ) {
                    JSONObject team = teams.getJSONObject(i);
                    String teamLink = team.getJSONObject(LINKS).getJSONObject(SELF).getString(HREF);
                    int teamId = Utilites.getLastUrlSegmentAsInt(teamLink);

                    //save data
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.TeamsEntry._ID, teamId);
                    cv.put(DatabaseContract.TeamsEntry.NAME_COL, team.getString(dataTeamName));
                    cv.put(DatabaseContract.TeamsEntry.SHORT_NAME_COL, team.getString(dataShortTeamName));
                    cv.put(DatabaseContract.TeamsEntry.CREST_URL_COL, team.getString(dataCrestUrl));
                    values.add(cv);
                }

                ContentValues[] insert_data = new ContentValues[values.size()];
                values.toArray(insert_data);

                int insertedRowCount = context.getContentResolver().bulkInsert(
                DatabaseContract.TeamsEntry.CONTENT_URI, insert_data);
                Log.d(LOG_TAG, "getLeagueData: saved "+insertedRowCount+" rows.");
            } catch (JSONException je) {
                Log.e(LOG_TAG, "getTeamData: "+je.getMessage());
            }

        } else Log.d(LOG_TAG, "getLeagueData: Result is null!");
    }

    private static void saveTeamData(int teamId, JSONObject data, Context context) {
        String dataTeamName = "name";
        String dataShortTeamName = "shortName";
        String dataCrestUrl = "crestUrl";
        try {
            Log.d(LOG_TAG, "saveTeamData: saving a team.");
            //save data
            ContentValues cv = new ContentValues(4);
            cv.put(DatabaseContract.TeamsEntry._ID, teamId);
            cv.put(DatabaseContract.TeamsEntry.NAME_COL, data.getString(dataTeamName));
            cv.put(DatabaseContract.TeamsEntry.SHORT_NAME_COL, data.getString(dataShortTeamName));
            cv.put(DatabaseContract.TeamsEntry.CREST_URL_COL, data.getString(dataCrestUrl));

            context.getContentResolver().insert(DatabaseContract.TeamsEntry.CONTENT_URI,cv);
            Log.d(LOG_TAG, "saveTeamData: saved a team. Yay");

        } catch (JSONException je) {
            Log.e(LOG_TAG, "processTeamData - Exception : "+je.getMessage() );
        }

    }
    /**
     * Check or get team
     * @param teamId
     * @param context
     */
    private void checkTeamOrGetLeague(int teamId, int leagueId, Context context) {

        String[] selection = {DatabaseContract.TeamsEntry._ID};
        String[] selectionArgs = {Integer.toString(teamId)};

        String selectionClause = DatabaseContract.TeamsEntry._ID + " = ?";

        Cursor result = context.getContentResolver().query(
                DatabaseContract.TeamsEntry.CONTENT_URI,
                selection,
                selectionClause,
                selectionArgs,
                null );

        if (!result.moveToFirst()) {
            getLeagueData(leagueId, context);
        }

        result.close();
    }

}
