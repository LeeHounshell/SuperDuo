package barqsoft.footballscores;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class DatabaseContract {
    public static final String SCORES_TABLE = "scores_table";

    // URL paths
    private static final String PATH_LEAGUE = "league";
    private static final String PATH_ID = "id";
    private static final String PATH_DATE = "date";
    private static final String PATH_SCORES = "scores";

    //URI data
    private static final String CONTENT_AUTHORITY = "barqsoft.footballscores";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Types
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORES;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORES;

    public static final class ScoresTable implements BaseColumns {
        //private final static String TAG = "LEE: <" + ScoresTable.class.getSimpleName() + ">";

        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";

        public static String[] getScoresTableColumnsForWidget() {
            return new String[] {
                    DATE_COL,
                    HOME_COL,
                    AWAY_COL,
                    HOME_GOALS_COL,
                    AWAY_GOALS_COL,
                    MATCH_ID,
                    LEAGUE_COL
            };
        }

        public static Uri buildScoreWithLeague() {
            //Log.v(TAG, "buildScoreWithLeague");
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_LEAGUE).build();
        }

        public static Uri buildScoreWithId() {
            //Log.v(TAG, "buildScoreWithId");
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_ID).build();
        }

        public static Uri buildScoreWithDate() {
            //Log.v(TAG, "buildScoreWithDate");
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_DATE).build();
        }
    }

    public static String getPathDate() {
        return PATH_DATE;
    }

}