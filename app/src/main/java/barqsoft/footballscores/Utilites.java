package barqsoft.footballscores;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilites {
    private static final String TAG = "Utilites";

    public static final int SERIE_A = 401;
    public static final int PREMIER_LEGAUE = 398;
    public static final int CHAMPIONS_LEAGUE = 405;
    public static final int PRIMERA_DIVISION = 399;
    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int BUNDESLIGA3 = 403;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int SEGUNDA = 400;
    public static final int PRIMEIRA_LIGA = 402;
    public static final int EREDIVISIE = 404;
    public static final String FOOTBALL_SCORES = "footballScores";
    public static final String LAST_UPDATED = "scoreLastUpdated";
    public static final String UNKNOWN_LEAGUE = "unknown";

    public static String getLeague(int league_num) {
        switch (league_num) {
            case SERIE_A:
                return "Seria A";
            case PREMIER_LEGAUE:
                return "Premier League";
            case CHAMPIONS_LEAGUE:
                return "UEFA Champions League";
            case PRIMERA_DIVISION:
                return "Primera Division";
            case PRIMEIRA_LIGA:
                return "Primeira Liga";
            case BUNDESLIGA1:
                return "Bundesliga 1";
            case BUNDESLIGA2:
                return "Bundesliga 2";
            case BUNDESLIGA3:
                return "Bundesliga 3";
            case LIGUE1:
                return "Ligue 1";
            case LIGUE2:
                return "Ligue 2";
            case SEGUNDA:
                return "Segunda Division";
            case EREDIVISIE:
                return "Eredivisie";
            default:
                return UNKNOWN_LEAGUE;
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            /*case "Arsenal London FC":
                return R.drawable.arsenal;
            case "Manchester United FC":
                return R.drawable.manchester_united;
            case "Swansea City":
                return R.drawable.swansea_city_afc;
            case "Leicester City":
                return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC":
                return R.drawable.everton_fc_logo1;
            case "West Ham United FC":
                return R.drawable.west_ham;
            case "Tottenham Hotspur FC":
                return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion":
                return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC":
                return R.drawable.sunderland;
            case "Stoke City FC":
                return R.drawable.stoke_city;*/
            default:
                return R.drawable.no_icon;
        }
    }

    public static void doToast(Context context, String text) {

        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
    public static String millisToDateTime(long millis) {
        return DateFormat.getDateTimeInstance().format(new Date(millis));
    }

    public static void logSyncs(Context context) {
        AccountManager acm
                = AccountManager.get(context);
        Account[] acct = null;

        SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();
        for (SyncAdapterType type : types) {
            Log.d(TAG, "--------------------");
            Log.d(TAG, "Authority: "+ type.authority + "-- Type: " + type.accountType);
//            acct = acm.getAccountsByType(type.accountType);
//            for (int i = 0; i < acct.length; i++) {
//                int p = ContentResolver.getIsSyncable(acct[i], type.authority);
//                Log.i(TAG, "account name: " + acct[i].name);
//                Log.i(TAG, "syncable: " + String.valueOf(p));
//            }
        }
    }

    //Based on a stackoverflow snippet
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int getLastUrlSegmentAsInt(String string) {
        Uri uri = Uri.parse(string);
        return Integer.parseInt(uri.getLastPathSegment());
    }

    public static JSONObject getJsonFromUrl(Uri uri, Context context) {
        
        Log.v(TAG, "The url we are looking at is: "+uri.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String jsonData = null;
        //Opening Connection
        try {
            URL fetch = new URL(uri.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", context.getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            jsonData = buffer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error Closing Stream");
                }
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject;
        } catch (JSONException je) {
            Log.e(TAG, "getJsonFromUrl, json parsing failure: "+je.getMessage());
        }
        return null;
    }
}