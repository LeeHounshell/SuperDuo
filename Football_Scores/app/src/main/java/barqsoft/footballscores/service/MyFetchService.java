package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.League;
import barqsoft.footballscores.R;


public class MyFetchService extends IntentService {
    private final static String TAG = "LEE: <" + MyFetchService.class.getSimpleName() + ">";

    public MyFetchService() {
        super("MyFetchService");
        //Log.v(TAG, "MyFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.v(TAG, "onHandleIntent");
        getData("n2");
        getData("p2");
    }

    private void getData(String timeFrame) {
        //Log.v(TAG, "getData");

        // Create fetch URL - issue resolving api.football-data.org on older < API 21 devices!
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        Log.v(TAG, "The url we are looking at is: " + fetch_build.toString()); //log spam

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String JSON_data = null;

        try {
            final int half_hour = 30 * 60;
            URL fetch = new URL(fetch_build.toString());
            connection = (HttpURLConnection) fetch.openConnection();

            // FIXED: request to follow redirects - this seems to solve networking issues on older devices < API 21
            connection.setInstanceFollowRedirects(true);
            // FIXED: cache the request data and use cache
            connection.setUseCaches(true);
            connection.setDefaultUseCaches(true);

            connection.setRequestMethod("GET");
            connection.addRequestProperty("Cache-Control", "max-age="+half_hour);
            connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));

            boolean redirect = false;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            Log.d(TAG, "===> HTTP STATUS CODE: " + status + ", redirect=" + redirect);

            connection.connect();
            Log.v(TAG, "connected!");

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) {
                //Log.w(TAG, "no data stream!");
                return; // Nothing to do.
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            // FIXED: use StringBuilder instead of StringBuffer
            StringBuilder buffer = new StringBuilder();
            String line;
            //Log.v(TAG, "reading..");
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
                buffer.append("\n");
            }
            if (buffer.length() == 0) {
                return; // Stream was empty.  No point in parsing.
            }
            JSON_data = buffer.toString();
            Log.d(TAG, "JSON_data=" + JSON_data);
        } catch (Exception e) {
            // possible UnknownHostException on older Android device?
            Log.e(TAG, "HttpURLConnection Exception - e=" + e.getMessage());
            e.printStackTrace();
        } finally {
            //Log.v(TAG, "close the stream");
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream - e=" + e.getMessage());
                }
            }
        }
        try {
            if (JSON_data != null) {
                //Log.v(TAG, "This bit is to check if the data contains any matches. If not, we call processJson on the dummy data");
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    //Log.v(TAG, "using dummy-data!");
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }
                //Log.v(TAG, "---> found match! - length="+matches.length());
                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void processJSONdata(String jsonData, Context mContext, boolean isReal) {
        //Log.v(TAG, "processJSONdata: isReal="+isReal+", jsonData="+jsonData);
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        // FIXED: updated league info - see league.java

        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        // FIXED: camel-case variables
        String league;
        String mDate;
        String mTime;
        String home;
        String away;
        String homeGoals;
        String awayGoals;
        String matchId;
        String matchDay;

        try {
            JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);
            //Log.v(TAG, "matches="+matches);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject matchData = matches.getJSONObject(i);
                league = matchData.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK, "");
                //Log.v(TAG, "checking league=" + league);
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (league.equals(String.valueOf(League.SERIE_A)) ||
                        league.equals(String.valueOf(League.PREMIER_LEGAUE)) ||
                        league.equals(String.valueOf(League.CHAMPIONS_LEAGUE)) ||
                        league.equals(String.valueOf(League.PRIMERA_DIVISION)) ||
                        league.equals(String.valueOf(League.BUNDESLIGA)) ||
                        league.equals(String.valueOf(League.BUNDESLIGA1)) ||
                        league.equals(String.valueOf(League.BUNDESLIGA2)) ||
                        league.equals(String.valueOf(League.LIGUE1)) ||
                        league.equals(String.valueOf(League.LIGUE2)) ||
                        league.equals(String.valueOf(League.PREMIER_LEAGUE)) ||
                        league.equals(String.valueOf(League.PRIMERA_DIVISION1)) ||
                        league.equals(String.valueOf(League.PRIMERA_DIVISION)) ||
                        league.equals(String.valueOf(League.SEGUNDA_DIVISION)) ||
                        league.equals(String.valueOf(League.SERIE_A1)) ||
                        league.equals(String.valueOf(League.PRIMERA_LIGA)) ||
                        league.equals(String.valueOf(League.BUNDESLIGA3)) ||
                        league.equals(String.valueOf(League.EREDIVISIE)))
                {
                    //Log.v(TAG, "--> league match <--");
                    matchId = matchData.getJSONObject(LINKS).getJSONObject(SELF).getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId = matchId + Integer.toString(i);
                    }

                    mDate = matchData.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    Locale locale = getResources().getConfiguration().locale;
                    SimpleDateFormat matchDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", locale);
                    matchDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = matchDate.parse(mDate + mTime);
                        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd:HH:mm", locale);
                        newDate.setTimeZone(TimeZone.getDefault());
                        mDate = newDate.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            //Log.v(TAG, "change dummy data");
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", locale);
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "parse error here! - e=" + e.getMessage());
                    }
                    home = matchData.getString(HOME_TEAM);
                    away = matchData.getString(AWAY_TEAM);
                    homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = matchData.getString(MATCH_DAY);
                    ContentValues matchValues = new ContentValues();
                    matchValues.put(DatabaseContract.ScoresTable.MATCH_ID, matchId);
                    matchValues.put(DatabaseContract.ScoresTable.DATE_COL, mDate);
                    matchValues.put(DatabaseContract.ScoresTable.TIME_COL, mTime);
                    matchValues.put(DatabaseContract.ScoresTable.HOME_COL, home);
                    matchValues.put(DatabaseContract.ScoresTable.AWAY_COL, away);
                    matchValues.put(DatabaseContract.ScoresTable.HOME_GOALS_COL, homeGoals);
                    matchValues.put(DatabaseContract.ScoresTable.AWAY_GOALS_COL, awayGoals);
                    matchValues.put(DatabaseContract.ScoresTable.LEAGUE_COL, league);
                    matchValues.put(DatabaseContract.ScoresTable.MATCH_DAY, matchDay);

                    //Log.v(TAG, matchId);
                    //Log.v(TAG, mDate);
                    //Log.v(TAG, mTime);
                    //Log.v(TAG, home);
                    //Log.v(TAG, away);
                    //Log.v(TAG, homeGoals);
                    //Log.v(TAG, awayGoals);

                    values.add(matchValues);
                }
            }
            int insertedData;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            insertedData = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            Log.d(TAG, "Successfully Inserted : " + String.valueOf(insertedData));
        } catch (JSONException e) {
            Log.e(TAG, "insert error here! - e=" + e.getMessage());
        }

        //Log.v(TAG, "processed");
    }

}
